package data.infrastructure.broker.ibkr

import application.logging.logger
import com.ib.client.*
import com.ib.client.protobuf.*
import domain.market.security.SecurityIdentifier
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.delay
import kotlinx.coroutines.withTimeout
import org.springframework.context.ApplicationEventPublisher
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Instant

class IbkrClient(
    private val event: ApplicationEventPublisher
) : EWrapper {

    //===========================================================//
    //===========================================================//
    // Private Field(s)

    private val signal = EJavaSignal()
    private val client = EClientSocket(this, signal)
    private var reader: EReader? = null
    private var readerThread: Thread? = null
    private val nextOrderId = AtomicInteger(-1)
    private var readySignal = CompletableDeferred<Unit>()
    private val logger = logger<IbkrClient>()
    private val marketDataRequestId = AtomicInteger(1_000)
    private val pendingPrices = ConcurrentHashMap<Int, CompletableDeferred<Double>>()

    private val accountSummaryRequestId = 2_000

    @Volatile
    private var latestAvailableFunds: Double? = null

    @Volatile
    private var latestNetLiquidation: Double? = null

    private val historicalDataRequestId = AtomicInteger(3_000)
    private val pendingHistoricalData = ConcurrentHashMap<Int, HistoricalDataRequest>()

    private val historicalEndDateFormatter = DateTimeFormatter.ofPattern("yyyyMMdd HH:mm:ss 'UTC'").withZone(ZoneOffset.UTC)

    //===========================================================//
    //===========================================================//
    // Public Method(s)

    suspend fun connect(host: String, port: Int, clientId: Int, maxAttempts: Int = 5) {
        if(client.isConnected && nextOrderId.get() >= 0) {
            logger.warn("IBKR client is already connected and ready")
            return
        }

        repeat(maxAttempts) { attempt ->
            try{
                logger.info("Connecting to IBKR attemp={}/{} host={}, port={} clientId={}",
                    attempt + 1, maxAttempts, host, port, clientId)

                nextOrderId.set(-1)
                readySignal = CompletableDeferred()

                if(client.isConnected) {
                    client.eDisconnect()
                }

                client.eConnect(host, port, clientId)

                if(!client.isConnected) {
                    throw IllegalStateException(
                        "Could not connect to IBKR Gateway"
                    )
                }

                startMessageReader()

                withTimeout(10_000.milliseconds) {
                    readySignal.await()
                }

                logger.info("IBKR client is ready")

                subscribeAccountSummary()
                return
            } catch(e: Exception) {
                logger.warn("IBKR conntection attempt {}/{} failed",
                    attempt + 1, maxAttempts, e)

                if(client.isConnected) {
                    client.eDisconnect()
                }

                readerThread?.interrupt()
                readerThread = null
                reader = null

                nextOrderId.set(-1)

                if(attempt == maxAttempts - 1) {
                    throw e
                }

                delay(10_000.milliseconds)
            }
        }
    }

    //===========================================================//

    fun isConnected(): Boolean = client.isConnected

    //===========================================================//

    fun disconnect() {
        if(!client.isConnected) {
            logger.debug("IBKR client is already disconnected")
            return
        }

        logger.info("Disconnecting from IBKR Gateway")
        client.cancelAccountSummary(
            accountSummaryRequestId
        )

        client.eDisconnect()

        readerThread?.interrupt()
        readerThread = null
        reader = null
        nextOrderId.set(-1)
        pendingPrices.values.forEach { pending -> pending.cancel() }
        pendingPrices.clear()
        pendingHistoricalData.values.forEach {pending ->pending.result.cancel()}
        pendingHistoricalData.clear()

    }

    //===========================================================//

    fun placeOrder(orderId: Int, contract: Contract, order: Order): Int {
        check(client.isConnected){
            "IBKR client is not connected"
        }

        check(nextOrderId.get() >= 0) {
            "IBKR has not provided a valid order id"
        }

        logger.info(
            "Submitting IBKR order orderId={} symbol={} action={} quantity={}",
            orderId,
            contract.symbol(),
            order.action(),
            order.totalQuantity()
        )

        client.placeOrder(
            orderId,
            contract,
            order
        )

        return orderId
    }

    //===========================================================//

    fun getAccountSummary(): IbkrPortfolioAccountSummary {
        val availableFunds = latestAvailableFunds
            ?: throw IllegalStateException(
                "IBKR AvailableFunds has not been received yet"
            )

        val netLiquidation = latestNetLiquidation
            ?: throw IllegalStateException(
                "IBKR NetLiquidation has not been received yet"
            )

        return IbkrPortfolioAccountSummary(
            availableCapital = availableFunds,
            netLiquidation = netLiquidation
        )
    }

    //===========================================================//

    suspend fun getCurrentPrice(ticker: String, currency: String): Double {
        check(client.isConnected){
            "IBKR client is not connected"
        }

        val requestId = marketDataRequestId.getAndIncrement()
        val result = CompletableDeferred<Double>()

        pendingPrices[requestId] = result
        val contract = Contract().apply{
            symbol(ticker)
            secType("STK")
            exchange("SMART")
            currency(currency)
        }

        client.reqMarketDataType(3)

        client.reqMktData(
            requestId,
            contract,
            "",
            false,
            false,
            null
        )

        return try {
            withTimeout(10_000.milliseconds){
                result.await()
            }
        } finally {
            pendingPrices.remove(requestId)
            client.cancelMktData(requestId)
            delay(1_000.milliseconds)
        }
    }

    //===========================================================//

    suspend fun getHistoricalData(identifier: SecurityIdentifier, from: Instant, to: Instant): List<IbkrHistoricalBar> {
        check(client.isConnected) {
            "IBKR client is not connected"
        }

        val requestId =
            historicalDataRequestId.getAndIncrement()

        val result =
            CompletableDeferred<List<IbkrHistoricalBar>>()

        pendingHistoricalData[requestId] =
            HistoricalDataRequest(
                result = result
            )

        val contract = Contract().apply {
            symbol(identifier.tickerSymbol)
            secType("STK")
            exchange("SMART")
            currency(identifier.currency)
        }

        val endDateTime =
            formatHistoricalEndDate(to)

        client.reqHistoricalData(
            requestId,
            contract,
            endDateTime,
            "1 W",
            "1 Day",
            "TRADES",
            1,
            1,
            false,
            null
        )

        return try {
            withTimeout(30_000.milliseconds) {
                result.await()
            }
        } finally {
            pendingHistoricalData.remove(requestId)
        }
    }

    //===========================================================//

    fun requestOpenOrders() {
        check(client.isConnected) {"IBKR client is not connected"}
        logger.debug("Requesting current IBKR open orders")
        client.reqOpenOrders()
    }

    //===========================================================//

    fun reserveOrderId(): Int {
        val orderId = nextOrderId.getAndIncrement()

        check(orderId >= 0) {
            "IBKR has not provided a valid order id"
        }

        return orderId
    }

    //===========================================================//
    //===========================================================//
    // Ibkr Method(s) - EWrapper

    override fun connectionClosed() = logger.info("IBKR connection closed")
    override fun connectAck() = logger.info("IBKR connection acknowledged")
    override fun orderStatusProtoBuf(message: OrderStatusProto.OrderStatus?) {
        if(message == null) {
            logger.warn("Received null IBKR orderStatus protobuf message")
            return
        }

        when(message.status){
            "ApiPending", "PendingSubmit" -> {
                event.publishEvent(IbkrEvent.OrderPendingEvent(message.orderId))
            }
            "PreSubmitted" -> {
                event.publishEvent(IbkrEvent.OrderPreSubmittedEvent(message.orderId))
            }
            "Submitted" -> {
                event.publishEvent(IbkrEvent.OrderSubmittedEvent(message.orderId))
            }
            "PendingCancel" -> {
                event.publishEvent(IbkrEvent.OrderPendingCancelEvent(message.orderId))
            }
            "Filled" -> {
                event.publishEvent(IbkrEvent.OrderFilledEvent(message.orderId, message.filled, message.avgFillPrice))
            }
            "ApiCancelled", "Cancelled" -> {
                event.publishEvent(IbkrEvent.OrderCancelledEvent(message.orderId))
            }
            "Inactive" -> {
                event.publishEvent(IbkrEvent.OrderInactiveEvent(message.orderId))
            }
        }
    }

    //===========================================================//

    override fun openOrderProtoBuf(message: OpenOrderProto.OpenOrder?) {
        if(message == null) {
            logger.warn("Received null IBKR openOrder protobuf message")
            return
        }

        logger.debug(
            "IBKR open order orderId={} symbol={} action={} status={}",
            message.orderId,
            message.contract.symbol,
            message.order.action,
            message.orderState.status
        )
    }

    //===========================================================//

    override fun openOrdersEndProtoBuf(message: OpenOrdersEndProto.OpenOrdersEnd?) {
        logger.debug("IBKR open-order snapshot completed")
    }

    //===========================================================//

    override fun errorProtoBuf(message: ErrorMessageProto.ErrorMessage?) {
        if(message == null) {
            logger.warn("Received null IBKR protobuf error message")
            return
        }

        when(message.errorCode) {
            2104, 2106, 2158 -> logger.info(
                "IBKR status requestId={} code={} message={}",
                message.id,
                message.errorCode,
                message.errorMsg
            )

            2107, 2108 -> logger.debug(
                "IBKR status requestId={} code={} message={}",
                message.id,
                message.errorCode,
                message.errorMsg
            )

            399 -> logger.warn(
                "IBKR order warning requestId={} code={} message={}",
                message.id,
                message.errorCode,
                message.errorMsg
            )

            else -> logger.error(
                "IBKR error requestId={} code={} message={}",
                message.id,
                message.errorCode,
                message.errorMsg
            )
        }
    }

    //===========================================================//

    override fun execDetailsProtoBuf(message: ExecutionDetailsProto.ExecutionDetails?) {
        if(message == null) {
            logger.warn("Received null IBKR executionDetails protobuf message")
            return
        }

        logger.info(
            "IBKR execution reqId={} symbol={} shares={} price={}",
            message.reqId,
            message.contract.symbol,
            message.execution.shares,
            message.execution.price
        )
    }

    //===========================================================//

    override fun execDetailsEndProtoBuf(message: ExecutionDetailsEndProto.ExecutionDetailsEnd?) {
        if(message == null) {
            logger.warn("Received null IBKR executionDetailsEnd protobuf message")
            return
        }

        logger.debug(
            "IBKR execution details completed reqId={}",
            message.reqId
        )
    }

    //===========================================================//

    override fun completedOrderProtoBuf(p0: CompletedOrderProto.CompletedOrder?) { throw UnsupportedOperationException() }
    override fun completedOrdersEndProtoBuf(p0: CompletedOrdersEndProto.CompletedOrdersEnd?) { throw UnsupportedOperationException() }
    override fun orderBoundProtoBuf(p0: OrderBoundProto.OrderBound?) { throw UnsupportedOperationException() }
    override fun contractDataProtoBuf(p0: ContractDataProto.ContractData?) { throw UnsupportedOperationException() }
    override fun bondContractDataProtoBuf(p0: ContractDataProto.ContractData?) { throw UnsupportedOperationException() }
    override fun contractDataEndProtoBuf(p0: ContractDataEndProto.ContractDataEnd?) { throw UnsupportedOperationException() }
    override fun tickPriceProtoBuf(message: TickPriceProto.TickPrice?) {
        if (message == null || message.price <= 0) return

        logger.info(
            "IBKR PRICE TICK reqId={} tickType={} price={}",
            message.reqId,
            message.tickType,
            message.price
        )

        when (message.tickType) {
            4,   // LAST
            68,  // DELAYED_LAST
            9,   // CLOSE
            75   // DELAYED_CLOSE
                -> {
                pendingPrices[message.reqId]
                    ?.takeIf { !it.isCompleted }
                    ?.complete(message.price)
            }
        }
    }

    override fun tickSizeProtoBuf(p0: TickSizeProto.TickSize?) { throw UnsupportedOperationException() }
    override fun tickOptionComputationProtoBuf(p0: TickOptionComputationProto.TickOptionComputation?) { throw UnsupportedOperationException() }
    override fun tickGenericProtoBuf(p0: TickGenericProto.TickGeneric?) { throw UnsupportedOperationException() }
    override fun tickStringProtoBuf(p0: TickStringProto.TickString?) { throw UnsupportedOperationException() }
    override fun tickSnapshotEndProtoBuf(p0: TickSnapshotEndProto.TickSnapshotEnd?) { throw UnsupportedOperationException() }
    override fun updateMarketDepthProtoBuf(p0: MarketDepthProto.MarketDepth?) { throw UnsupportedOperationException() }
    override fun updateMarketDepthL2ProtoBuf(p0: MarketDepthL2Proto.MarketDepthL2?) { throw UnsupportedOperationException() }
    override fun marketDataTypeProtoBuf(p0: MarketDataTypeProto.MarketDataType?) { throw UnsupportedOperationException() }
    override fun tickReqParamsProtoBuf(p0: TickReqParamsProto.TickReqParams?) { throw UnsupportedOperationException() }
    override fun updateAccountValueProtoBuf(p0: AccountValueProto.AccountValue?) { throw UnsupportedOperationException() }
    override fun updatePortfolioProtoBuf(p0: PortfolioValueProto.PortfolioValue?) { throw UnsupportedOperationException() }
    override fun updateAccountTimeProtoBuf(p0: AccountUpdateTimeProto.AccountUpdateTime?) { throw UnsupportedOperationException() }
    override fun accountDataEndProtoBuf(p0: AccountDataEndProto.AccountDataEnd?) { throw UnsupportedOperationException() }
    override fun managedAccountsProtoBuf(message: ManagedAccountsProto.ManagedAccounts?) {
        if(message == null) {
            logger.warn("Received null IBKR managedAccounts protobuf message")
            return
        }

        logger.info(
            "IBKR managed accounts account={}",
            message.accountsList
        )
    }

    override fun positionProtoBuf(p0: PositionProto.Position?) { throw UnsupportedOperationException() }
    override fun positionEndProtoBuf(p0: PositionEndProto.PositionEnd?) { throw UnsupportedOperationException() }
    override fun accountSummaryProtoBuf(message: AccountSummaryProto.AccountSummary?) {
        if (message == null) return

        val value = message.value.toDoubleOrNull()
            ?: return

        when (message.tag) {

            "AvailableFunds" -> {
                latestAvailableFunds = value

                logger.debug(
                    "IBKR AvailableFunds updated: {}",
                    value
                )
            }

            "NetLiquidation" -> {
                latestNetLiquidation = value

                logger.debug(
                    "IBKR NetLiquidation updated: {}",
                    value
                )
            }
        }
    }

    override fun accountSummaryEndProtoBuf(p0: AccountSummaryEndProto.AccountSummaryEnd?) {
        logger.debug("IBKR account summary snapshot completed")
    }

    override fun positionMultiProtoBuf(p0: PositionMultiProto.PositionMulti?) { throw UnsupportedOperationException() }
    override fun positionMultiEndProtoBuf(p0: PositionMultiEndProto.PositionMultiEnd?) { throw UnsupportedOperationException() }
    override fun accountUpdateMultiProtoBuf(p0: AccountUpdateMultiProto.AccountUpdateMulti?) { throw UnsupportedOperationException() }
    override fun accountUpdateMultiEndProtoBuf(p0: AccountUpdateMultiEndProto.AccountUpdateMultiEnd?) { throw UnsupportedOperationException() }
    override fun historicalDataProtoBuf(message: HistoricalDataProto.HistoricalData?) {
        if(message == null) return
        val request = pendingHistoricalData[message.reqId]?:return

        message.historicalDataBarsList.forEach {bar ->
            request.bars.add(
                IbkrHistoricalBar(
                    timestamp = bar.date,
                    price = bar.close
                )
            )
        }
    }

    override fun historicalDataUpdateProtoBuf(p0: HistoricalDataUpdateProto.HistoricalDataUpdate?) { throw UnsupportedOperationException() }
    override fun historicalDataEndProtoBuf(message: HistoricalDataEndProto.HistoricalDataEnd?) {
        if(message == null) {
            return
        }

        val request = pendingHistoricalData[message.reqId]
            ?: return

        if(!request.result.isCompleted) {
            request.result.complete(
                request.bars.toList()
            )
        }
    }

    override fun realTimeBarTickProtoBuf(p0: RealTimeBarTickProto.RealTimeBarTick?) { throw UnsupportedOperationException() }
    override fun headTimestampProtoBuf(p0: HeadTimestampProto.HeadTimestamp?) { throw UnsupportedOperationException() }
    override fun histogramDataProtoBuf(p0: HistogramDataProto.HistogramData?) { throw UnsupportedOperationException() }
    override fun historicalTicksProtoBuf(p0: HistoricalTicksProto.HistoricalTicks?) { throw UnsupportedOperationException() }
    override fun historicalTicksBidAskProtoBuf(p0: HistoricalTicksBidAskProto.HistoricalTicksBidAsk?) { throw UnsupportedOperationException() }
    override fun historicalTicksLastProtoBuf(p0: HistoricalTicksLastProto.HistoricalTicksLast?) { throw UnsupportedOperationException() }
    override fun tickByTickDataProtoBuf(p0: TickByTickDataProto.TickByTickData?) { throw UnsupportedOperationException() }
    override fun updateNewsBulletinProtoBuf(p0: NewsBulletinProto.NewsBulletin?) { throw UnsupportedOperationException() }
    override fun newsArticleProtoBuf(p0: NewsArticleProto.NewsArticle?) { throw UnsupportedOperationException() }
    override fun newsProvidersProtoBuf(p0: NewsProvidersProto.NewsProviders?) { throw UnsupportedOperationException() }
    override fun historicalNewsProtoBuf(p0: HistoricalNewsProto.HistoricalNews?) { throw UnsupportedOperationException() }
    override fun historicalNewsEndProtoBuf(p0: HistoricalNewsEndProto.HistoricalNewsEnd?) { throw UnsupportedOperationException() }
    override fun wshMetaDataProtoBuf(p0: WshMetaDataProto.WshMetaData?) { throw UnsupportedOperationException() }
    override fun wshEventDataProtoBuf(p0: WshEventDataProto.WshEventData?) { throw UnsupportedOperationException() }
    override fun tickNewsProtoBuf(p0: TickNewsProto.TickNews?) { throw UnsupportedOperationException() }
    override fun scannerParametersProtoBuf(p0: ScannerParametersProto.ScannerParameters?) { throw UnsupportedOperationException() }
    override fun scannerDataProtoBuf(p0: ScannerDataProto.ScannerData?) { throw UnsupportedOperationException() }
    override fun fundamentalsDataProtoBuf(p0: FundamentalsDataProto.FundamentalsData?) { throw UnsupportedOperationException() }
    override fun pnlProtoBuf(p0: PnLProto.PnL?) { throw UnsupportedOperationException() }
    override fun pnlSingleProtoBuf(p0: PnLSingleProto.PnLSingle?) { throw UnsupportedOperationException() }
    override fun receiveFAProtoBuf(p0: ReceiveFAProto.ReceiveFA?) { throw UnsupportedOperationException() }
    override fun replaceFAEndProtoBuf(p0: ReplaceFAEndProto.ReplaceFAEnd?) { throw UnsupportedOperationException() }
    override fun commissionAndFeesReportProtoBuf(p0: CommissionAndFeesReportProto.CommissionAndFeesReport?) { throw UnsupportedOperationException() }
    override fun historicalScheduleProtoBuf(p0: HistoricalScheduleProto.HistoricalSchedule?) { throw UnsupportedOperationException() }
    override fun rerouteMarketDataRequestProtoBuf(p0: RerouteMarketDataRequestProto.RerouteMarketDataRequest?) { throw UnsupportedOperationException() }
    override fun rerouteMarketDepthRequestProtoBuf(p0: RerouteMarketDepthRequestProto.RerouteMarketDepthRequest?) { throw UnsupportedOperationException() }
    override fun secDefOptParameterProtoBuf(p0: SecDefOptParameterProto.SecDefOptParameter?) { throw UnsupportedOperationException() }
    override fun secDefOptParameterEndProtoBuf(p0: SecDefOptParameterEndProto.SecDefOptParameterEnd?) { throw UnsupportedOperationException() }
    override fun softDollarTiersProtoBuf(p0: SoftDollarTiersProto.SoftDollarTiers?) { throw UnsupportedOperationException() }
    override fun familyCodesProtoBuf(p0: FamilyCodesProto.FamilyCodes?) { throw UnsupportedOperationException() }
    override fun symbolSamplesProtoBuf(p0: SymbolSamplesProto.SymbolSamples?) { throw UnsupportedOperationException() }
    override fun smartComponentsProtoBuf(p0: SmartComponentsProto.SmartComponents?) { throw UnsupportedOperationException() }
    override fun marketRuleProtoBuf(p0: MarketRuleProto.MarketRule?) { throw UnsupportedOperationException() }
    override fun userInfoProtoBuf(p0: UserInfoProto.UserInfo?) { throw UnsupportedOperationException() }
    override fun nextValidIdProtoBuf(message: NextValidIdProto.NextValidId?) {
        if(message == null) {
            logger.warn("Received null IBKR nextValidId protobuf message")
            return
        }

        nextOrderId.updateAndGet { current ->
            maxOf(current, message.orderId)
        }

        logger.info("IBKR next valid orderId={}", message.orderId)

        if(!readySignal.isCompleted) {
            readySignal.complete(Unit)
        }
    }

    override fun currentTimeProtoBuf(p0: CurrentTimeProto.CurrentTime?) { throw UnsupportedOperationException() }
    override fun currentTimeInMillisProtoBuf(p0: CurrentTimeInMillisProto.CurrentTimeInMillis?) { throw UnsupportedOperationException() }
    override fun verifyMessageApiProtoBuf(p0: VerifyMessageApiProto.VerifyMessageApi?) { throw UnsupportedOperationException() }
    override fun verifyCompletedProtoBuf(p0: VerifyCompletedProto.VerifyCompleted?) { throw UnsupportedOperationException() }
    override fun displayGroupListProtoBuf(p0: DisplayGroupListProto.DisplayGroupList?) { throw UnsupportedOperationException() }
    override fun displayGroupUpdatedProtoBuf(p0: DisplayGroupUpdatedProto.DisplayGroupUpdated?) { throw UnsupportedOperationException() }
    override fun marketDepthExchangesProtoBuf(p0: MarketDepthExchangesProto.MarketDepthExchanges?) { throw UnsupportedOperationException() }
    override fun configResponseProtoBuf(p0: ConfigResponseProto.ConfigResponse?) { throw UnsupportedOperationException() }
    override fun updateConfigResponseProtoBuf(p0: UpdateConfigResponseProto.UpdateConfigResponse?) { throw UnsupportedOperationException() }

    //===========================================================//
    //===========================================================//
    // Private Method(s)

    private fun subscribeAccountSummary() {
        check(client.isConnected) {
            "IBKR client is not connected"
        }

        client.reqAccountSummary(
            accountSummaryRequestId,
            "All",
            "AvailableFunds,NetLiquidation"
        )

        logger.info("Subscribed to IBKR account summary")
    }

    //===========================================================//

    private fun startMessageReader() {
        logger.debug("Starting IBKR message reader")

        reader = EReader(client, signal)

        reader?.start()

        readerThread = Thread {
            while(client.isConnected){
                try{
                    signal.waitForSignal()
                    reader?.processMsgs()
                } catch(e: InterruptedException){
                    Thread.currentThread().interrupt()
                    logger.debug("IBKR reader thread interrupted")
                    break
                } catch(e: Exception){
                    logger.error("Unexpected error in IBKR reader thread", e)
                }
            }
        }.apply {
            name = "ibkr-reader"
            isDaemon = true
            start()
        }
    }

    //===========================================================//



    //===========================================================//

    private fun formatHistoricalEndDate(to: Instant): String {
        val javaInstant = java.time.Instant.ofEpochMilli(
            to.toEpochMilliseconds()
        )

        return historicalEndDateFormatter.format(javaInstant)
    }

    //===========================================================//
    //===========================================================//
    // LEGACY

    override fun tickPrice(tickerId: Int, field: Int, price: Double, attrib: TickAttrib?) { throw UnsupportedOperationException() }
    override fun tickSize(p0: Int, p1: Int, p2: Decimal?) { throw UnsupportedOperationException() }
    override fun tickOptionComputation(p0: Int, p1: Int, p2: Int, p3: Double, p4: Double, p5: Double, p6: Double, p7: Double, p8: Double, p9: Double, p10: Double) { throw UnsupportedOperationException() }
    override fun tickGeneric(p0: Int, p1: Int, p2: Double) { throw UnsupportedOperationException() }
    override fun tickString(p0: Int, p1: Int, p2: String?) { throw UnsupportedOperationException() }
    override fun tickEFP(p0: Int, p1: Int, p2: Double, p3: String?, p4: Double, p5: Int, p6: String?, p7: Double, p8: Double) { throw UnsupportedOperationException() }
    override fun orderStatus(p0: Int, p1: String?, p2: Decimal?, p3: Decimal?, p4: Double, p5: Long, p6: Int, p7: Double, p8: Int, p9: String?, p10: Double) { throw UnsupportedOperationException() }
    override fun openOrder(orderId: Int, contract: Contract?, order: Order?, orderState: OrderState?) { throw UnsupportedOperationException() }
    override fun openOrderEnd() { throw UnsupportedOperationException() }
    override fun updateAccountValue(p0: String?, p1: String?, p2: String?, p3: String?) { throw UnsupportedOperationException() }
    override fun updatePortfolio(p0: Contract?, p1: Decimal?, p2: Double, p3: Double, p4: Double, p5: Double, p6: Double, p7: String?) { throw UnsupportedOperationException() }
    override fun updateAccountTime(p0: String?) { throw UnsupportedOperationException() }
    override fun accountDownloadEnd(p0: String?) { throw UnsupportedOperationException() }
    override fun nextValidId(orderId: Int) { throw UnsupportedOperationException() }
    override fun contractDetails(p0: Int, p1: ContractDetails?) { throw UnsupportedOperationException() }
    override fun bondContractDetails(p0: Int, p1: ContractDetails?) { throw UnsupportedOperationException() }
    override fun contractDetailsEnd(p0: Int) { throw UnsupportedOperationException() }
    override fun execDetails(reqId: Int, contract: Contract?, execution: Execution?) { throw UnsupportedOperationException() }
    override fun execDetailsEnd(reqId: Int) { throw UnsupportedOperationException() }
    override fun updateMktDepth(p0: Int, p1: Int, p2: Int, p3: Int, p4: Double, p5: Decimal?) { throw UnsupportedOperationException() }
    override fun updateMktDepthL2(p0: Int, p1: Int, p2: String?, p3: Int, p4: Int, p5: Double, p6: Decimal?, p7: Boolean) { throw UnsupportedOperationException() }
    override fun updateNewsBulletin(p0: Int, p1: Int, p2: String?, p3: String?) { throw UnsupportedOperationException() }
    override fun managedAccounts(p0: String?) { throw UnsupportedOperationException() }
    override fun receiveFA(p0: Int, p1: String?) { throw UnsupportedOperationException() }
    override fun historicalData(p0: Int, p1: Bar?) { throw UnsupportedOperationException() }
    override fun scannerParameters(p0: String?) { throw UnsupportedOperationException() }
    override fun scannerData(p0: Int, p1: Int, p2: ContractDetails?, p3: String?, p4: String?, p5: String?, p6: String?) { throw UnsupportedOperationException() }
    override fun scannerDataEnd(p0: Int) { throw UnsupportedOperationException() }
    override fun realtimeBar(p0: Int, p1: Long, p2: Double, p3: Double, p4: Double, p5: Double, p6: Decimal?, p7: Decimal?, p8: Int) { throw UnsupportedOperationException() }
    override fun currentTime(p0: Long) { throw UnsupportedOperationException() }
    override fun fundamentalData(p0: Int, p1: String?) { throw UnsupportedOperationException() }
    override fun deltaNeutralValidation(p0: Int, p1: DeltaNeutralContract?) { throw UnsupportedOperationException() }
    override fun tickSnapshotEnd(p0: Int) { throw UnsupportedOperationException() }
    override fun marketDataType(p0: Int, p1: Int) { throw UnsupportedOperationException() }
    override fun commissionAndFeesReport(p0: CommissionAndFeesReport?) { throw UnsupportedOperationException() }
    override fun position(p0: String?, p1: Contract?, p2: Decimal?, p3: Double) { throw UnsupportedOperationException() }
    override fun positionEnd() { throw UnsupportedOperationException() }
    override fun accountSummary(p0: Int, p1: String?, p2: String?, p3: String?, p4: String?) { throw UnsupportedOperationException() }
    override fun accountSummaryEnd(p0: Int) { throw UnsupportedOperationException() }
    override fun verifyMessageAPI(p0: String?) { throw UnsupportedOperationException() }
    override fun verifyCompleted(p0: Boolean, p1: String?) { throw UnsupportedOperationException() }
    override fun verifyAndAuthMessageAPI(p0: String?, p1: String?) { throw UnsupportedOperationException() }
    override fun verifyAndAuthCompleted(p0: Boolean, p1: String?) { throw UnsupportedOperationException() }
    override fun displayGroupList(p0: Int, p1: String?) { throw UnsupportedOperationException() }
    override fun displayGroupUpdated(p0: Int, p1: String?) { throw UnsupportedOperationException() }
    override fun error(exception: Exception?) { throw UnsupportedOperationException() }
    override fun error(message: String?) { throw UnsupportedOperationException() }
    override fun error(p0: Int, p1: Long, p2: Int, p3: String?, p4: String?) { throw UnsupportedOperationException() }
    override fun positionMulti(p0: Int, p1: String?, p2: String?, p3: Contract?, p4: Decimal?, p5: Double) { throw UnsupportedOperationException() }
    override fun positionMultiEnd(p0: Int) { throw UnsupportedOperationException() }
    override fun accountUpdateMulti(p0: Int, p1: String?, p2: String?, p3: String?, p4: String?, p5: String?) { throw UnsupportedOperationException() }
    override fun accountUpdateMultiEnd(p0: Int) { throw UnsupportedOperationException() }
    override fun securityDefinitionOptionalParameter(p0: Int, p1: String?, p2: Int, p3: String?, p4: String?, p5: Set<String?>?, p6: Set<Double?>?) { throw UnsupportedOperationException() }
    override fun securityDefinitionOptionalParameterEnd(p0: Int) { throw UnsupportedOperationException() }
    override fun softDollarTiers(p0: Int, p1: Array<out SoftDollarTier?>?) { throw UnsupportedOperationException() }
    override fun familyCodes(p0: Array<out FamilyCode?>?) { throw UnsupportedOperationException() }
    override fun symbolSamples(p0: Int, p1: Array<out ContractDescription?>?) { throw UnsupportedOperationException() }
    override fun historicalDataEnd(p0: Int, p1: String?, p2: String?) { throw UnsupportedOperationException() }
    override fun mktDepthExchanges(p0: Array<out DepthMktDataDescription?>?) { throw UnsupportedOperationException() }
    override fun tickNews(p0: Int, p1: Long, p2: String?, p3: String?, p4: String?, p5: String?) { throw UnsupportedOperationException() }
    override fun smartComponents(p0: Int, p1: Map<Int?, Map.Entry<String?, Char?>?>?) { throw UnsupportedOperationException() }
    override fun tickReqParams(p0: Int, p1: Double, p2: String?, p3: Int) { throw UnsupportedOperationException() }
    override fun newsProviders(p0: Array<out NewsProvider?>?) { throw UnsupportedOperationException() }
    override fun newsArticle(p0: Int, p1: Int, p2: String?) { throw UnsupportedOperationException() }
    override fun historicalNews(p0: Int, p1: String?, p2: String?, p3: String?, p4: String?) { throw UnsupportedOperationException() }
    override fun historicalNewsEnd(p0: Int, p1: Boolean) { throw UnsupportedOperationException() }
    override fun headTimestamp(p0: Int, p1: String?) { throw UnsupportedOperationException() }
    override fun histogramData(p0: Int, p1: List<HistogramEntry?>?) { throw UnsupportedOperationException() }
    override fun historicalDataUpdate(p0: Int, p1: Bar?) { throw UnsupportedOperationException() }
    override fun rerouteMktDataReq(p0: Int, p1: Int, p2: String?) { throw UnsupportedOperationException() }
    override fun rerouteMktDepthReq(p0: Int, p1: Int, p2: String?) { throw UnsupportedOperationException() }
    override fun marketRule(p0: Int, p1: Array<out PriceIncrement?>?) { throw UnsupportedOperationException() }
    override fun pnl(p0: Int, p1: Double, p2: Double, p3: Double) { throw UnsupportedOperationException() }
    override fun pnlSingle(p0: Int, p1: Decimal?, p2: Double, p3: Double, p4: Double, p5: Double) { throw UnsupportedOperationException() }
    override fun historicalTicks(p0: Int, p1: List<HistoricalTick?>?, p2: Boolean) { throw UnsupportedOperationException() }
    override fun historicalTicksBidAsk(p0: Int, p1: List<HistoricalTickBidAsk?>?, p2: Boolean) { throw UnsupportedOperationException() }
    override fun historicalTicksLast(p0: Int, p1: List<HistoricalTickLast?>?, p2: Boolean) { throw UnsupportedOperationException() }
    override fun tickByTickAllLast(p0: Int, p1: Int, p2: Long, p3: Double, p4: Decimal?, p5: TickAttribLast?, p6: String?, p7: String?) { throw UnsupportedOperationException() }
    override fun tickByTickBidAsk(p0: Int, p1: Long, p2: Double, p3: Double, p4: Decimal?, p5: Decimal?, p6: TickAttribBidAsk?) { throw UnsupportedOperationException() }
    override fun tickByTickMidPoint(p0: Int, p1: Long, p2: Double) { throw UnsupportedOperationException() }
    override fun orderBound(p0: Long, p1: Int, p2: Int) { throw UnsupportedOperationException() }
    override fun completedOrder(p0: Contract?, p1: Order?, p2: OrderState?) { throw UnsupportedOperationException() }
    override fun completedOrdersEnd() { throw UnsupportedOperationException() }
    override fun replaceFAEnd(p0: Int, p1: String?) { throw UnsupportedOperationException() }
    override fun wshMetaData(p0: Int, p1: String?) { throw UnsupportedOperationException() }
    override fun wshEventData(p0: Int, p1: String?) { throw UnsupportedOperationException() }
    override fun historicalSchedule(p0: Int, p1: String?, p2: String?, p3: String?, p4: List<HistoricalSession?>?) { throw UnsupportedOperationException() }
    override fun userInfo(p0: Int, p1: String?) { throw UnsupportedOperationException() }
    override fun currentTimeInMillis(p0: Long) { throw UnsupportedOperationException() }
}

