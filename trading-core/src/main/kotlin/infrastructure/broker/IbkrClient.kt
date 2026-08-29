package infrastructure.broker

import com.ib.client.*
import com.ib.client.protobuf.*
import java.lang.Exception
import application.logging.logger
import domain.market.security.SecurityIdentifier
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.delay
import kotlinx.coroutines.withTimeout
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Component
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger
import kotlin.time.Instant

@Component
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
    private val accountSummaryRequestId = AtomicInteger(2_000)
    private val pendingAccountSummaryRequests = ConcurrentHashMap<Int, CompletableDeferred<Double>>()
    private val historicalDataRequestId = AtomicInteger(3_000)
    private val pendingHistoricalData = ConcurrentHashMap<Int, HistoricalDataRequest>()

    //===========================================================//
    //===========================================================//
    // Public Method(s)

    suspend fun connect(host: String, port: Int, clientId: Int, maxAttempts: Int = 3) {
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

                withTimeout(10_000) {
                    readySignal.await()
                }

                logger.info("IBKR client is ready")
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

                delay(2_000)
            }
        }
    }

    fun disconnect(){
        if(!client.isConnected) {
            logger.debug("IBKR client is already disconnected")
            return
        }

        logger.info("Disconnecting from IBKR Gateway")

        client.eDisconnect()

        readerThread?.interrupt()
        readerThread = null
        reader = null
        nextOrderId.set(-1)
        pendingPrices.values.forEach { pending -> pending.cancel() }
        pendingPrices.clear()
        pendingAccountSummaryRequests.values.forEach { pending -> pending.cancel() }
        pendingAccountSummaryRequests.clear()
        pendingHistoricalData.values.forEach {pending ->pending.result.cancel()}
        pendingHistoricalData.clear()

    }

    fun isConnected(): Boolean{
        return client.isConnected
    }
    fun getNextOrderId(): Int{
        return nextOrderId.getAndIncrement()
    }

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
            withTimeout(10_000){
                result.await()
            }
        } finally {
            pendingPrices.remove(requestId)
            client.cancelMktData(requestId)
        }
    }

    suspend fun getAvailableCapital(): Double{
        check(client.isConnected){ "IBKR client is not connected" }

        val requestId = accountSummaryRequestId.getAndIncrement()
        val result = CompletableDeferred<Double>()

        pendingAccountSummaryRequests[requestId] = result

        client.reqAccountSummary(requestId, "All", "AvailableFunds")
        return try{
            withTimeout(10_000){
                result.await()
            }
        }finally {
            pendingAccountSummaryRequests.remove(requestId)
            client.cancelAccountSummary(requestId)
        }

    }

    suspend fun getHistoricalData(identifier: SecurityIdentifier, from: Instant, to: Instant): List<IbkrHistoricalBar> {
        check(client.isConnected){"IBKR client is not connected"}

        val requestId = historicalDataRequestId.getAndIncrement()
        val result = CompletableDeferred<List<IbkrHistoricalBar>>()

        pendingHistoricalData[requestId] = HistoricalDataRequest(result = result)

        val contract = Contract().apply{
            symbol(identifier.tickerSymbol)
            secType("STK")
            exchange("SMART")
            currency(identifier.currency)
        }

        val endDateTime = formatHistoricalEndDate(to)
        val duration = calculateHistoricalDuration(from, to)

        client.reqHistoricalData(
            requestId,
            contract,
            endDateTime,
            duration,
            "1 day",
            "TRADES",
            1,
            1,
            false,
            null
        )

        return try {
            withTimeout(10_000){
                result.await()
            }
        } finally {
            pendingHistoricalData.remove(requestId)
        }
    }

    fun requestOpenOrders() {
        check(client.isConnected) {"IBKR client is not connected"}
        logger.debug("Requesting current IBKR open orders")
        client.reqOpenOrders()
    }

    //===========================================================//
    //===========================================================//
    // Private Method(s)

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

    private data class HistoricalDataRequest(
        val bars: MutableList<IbkrHistoricalBar> = mutableListOf(),
        val result: CompletableDeferred<List<IbkrHistoricalBar>>
    )

    private val historicalEndDateFormatter = DateTimeFormatter.ofPattern("yyyyMMdd HH:mm:ss 'UTC'").withZone(ZoneOffset.UTC)

    private fun formatHistoricalEndDate(to: Instant): String {
        val javaInstant = java.time.Instant.ofEpochMilli(
            to.toEpochMilliseconds()
        )

        return historicalEndDateFormatter.format(javaInstant)
    }

    private fun calculateHistoricalDuration(
        from: Instant,
        to: Instant
    ): String {
        val seconds = (to - from).inWholeSeconds.coerceAtLeast(1)
        val days = ((seconds + 86_399) / 86_400).coerceAtLeast(1)

        return if(days <= 365) {
            "$days D"
        }
        else {
            val years = (days + 364) / 365
            "$years Y"
        }
    }

    //===========================================================//
    //===========================================================//
    // LEGACY

    override fun tickPrice(tickerId: Int, field: Int, price: Double, attrib: TickAttrib?) {}
    override fun tickSize(p0: Int, p1: Int, p2: Decimal?) {}
    override fun tickOptionComputation(p0: Int, p1: Int, p2: Int, p3: Double, p4: Double, p5: Double, p6: Double, p7: Double, p8: Double, p9: Double, p10: Double) {}
    override fun tickGeneric(p0: Int, p1: Int, p2: Double) {}
    override fun tickString(p0: Int, p1: Int, p2: String?) {}
    override fun tickEFP(p0: Int, p1: Int, p2: Double, p3: String?, p4: Double, p5: Int, p6: String?, p7: Double, p8: Double) {}
    override fun orderStatus(p0: Int, p1: String?, p2: Decimal?, p3: Decimal?, p4: Double, p5: Long, p6: Int, p7: Double, p8: Int, p9: String?, p10: Double) {}
    override fun openOrder(orderId: Int, contract: Contract?, order: Order?, orderState: OrderState?) {}
    override fun openOrderEnd() {}
    override fun updateAccountValue(p0: String?, p1: String?, p2: String?, p3: String?) {}
    override fun updatePortfolio(p0: Contract?, p1: Decimal?, p2: Double, p3: Double, p4: Double, p5: Double, p6: Double, p7: String?) {}
    override fun updateAccountTime(p0: String?) {}
    override fun accountDownloadEnd(p0: String?) {}
    override fun nextValidId(orderId: Int) {}
    override fun contractDetails(p0: Int, p1: ContractDetails?) {}
    override fun bondContractDetails(p0: Int, p1: ContractDetails?) {}
    override fun contractDetailsEnd(p0: Int) {}
    override fun execDetails(reqId: Int, contract: Contract?, execution: Execution?) {}
    override fun execDetailsEnd(reqId: Int) {}
    override fun updateMktDepth(p0: Int, p1: Int, p2: Int, p3: Int, p4: Double, p5: Decimal?) {}
    override fun updateMktDepthL2(p0: Int, p1: Int, p2: String?, p3: Int, p4: Int, p5: Double, p6: Decimal?, p7: Boolean) {}
    override fun updateNewsBulletin(p0: Int, p1: Int, p2: String?, p3: String?) {}
    override fun managedAccounts(p0: String?) {}
    override fun receiveFA(p0: Int, p1: String?) {}
    override fun historicalData(p0: Int, p1: Bar?) {}
    override fun scannerParameters(p0: String?) {}
    override fun scannerData(p0: Int, p1: Int, p2: ContractDetails?, p3: String?, p4: String?, p5: String?, p6: String?) {}
    override fun scannerDataEnd(p0: Int) {}
    override fun realtimeBar(p0: Int, p1: Long, p2: Double, p3: Double, p4: Double, p5: Double, p6: Decimal?, p7: Decimal?, p8: Int) {}
    override fun currentTime(p0: Long) {}
    override fun fundamentalData(p0: Int, p1: String?) {}
    override fun deltaNeutralValidation(p0: Int, p1: DeltaNeutralContract?) {}
    override fun tickSnapshotEnd(p0: Int) {}
    override fun marketDataType(p0: Int, p1: Int) {}
    override fun commissionAndFeesReport(p0: CommissionAndFeesReport?) {}
    override fun position(p0: String?, p1: Contract?, p2: Decimal?, p3: Double) {}
    override fun positionEnd() {}
    override fun accountSummary(p0: Int, p1: String?, p2: String?, p3: String?, p4: String?) {}
    override fun accountSummaryEnd(p0: Int) {}
    override fun verifyMessageAPI(p0: String?) {}
    override fun verifyCompleted(p0: Boolean, p1: String?) {}
    override fun verifyAndAuthMessageAPI(p0: String?, p1: String?) {}
    override fun verifyAndAuthCompleted(p0: Boolean, p1: String?) {}
    override fun displayGroupList(p0: Int, p1: String?) {}
    override fun displayGroupUpdated(p0: Int, p1: String?) {}
    override fun error(exception: Exception?) {}
    override fun error(message: String?) {}
    override fun error(p0: Int, p1: Long, p2: Int, p3: String?, p4: String?) {}
    override fun positionMulti(p0: Int, p1: String?, p2: String?, p3: Contract?, p4: Decimal?, p5: Double) {}
    override fun positionMultiEnd(p0: Int) {}
    override fun accountUpdateMulti(p0: Int, p1: String?, p2: String?, p3: String?, p4: String?, p5: String?) {}
    override fun accountUpdateMultiEnd(p0: Int) {}
    override fun securityDefinitionOptionalParameter(p0: Int, p1: String?, p2: Int, p3: String?, p4: String?, p5: Set<String?>?, p6: Set<Double?>?) {}
    override fun securityDefinitionOptionalParameterEnd(p0: Int) {}
    override fun softDollarTiers(p0: Int, p1: Array<out SoftDollarTier?>?) {}
    override fun familyCodes(p0: Array<out FamilyCode?>?) {}
    override fun symbolSamples(p0: Int, p1: Array<out ContractDescription?>?) {}
    override fun historicalDataEnd(p0: Int, p1: String?, p2: String?) {}
    override fun mktDepthExchanges(p0: Array<out DepthMktDataDescription?>?) {}
    override fun tickNews(p0: Int, p1: Long, p2: String?, p3: String?, p4: String?, p5: String?) {}
    override fun smartComponents(p0: Int, p1: Map<Int?, Map.Entry<String?, Char?>?>?) {}
    override fun tickReqParams(p0: Int, p1: Double, p2: String?, p3: Int) {}
    override fun newsProviders(p0: Array<out NewsProvider?>?) {}
    override fun newsArticle(p0: Int, p1: Int, p2: String?) {}
    override fun historicalNews(p0: Int, p1: String?, p2: String?, p3: String?, p4: String?) {}
    override fun historicalNewsEnd(p0: Int, p1: Boolean) {}
    override fun headTimestamp(p0: Int, p1: String?) {}
    override fun histogramData(p0: Int, p1: List<HistogramEntry?>?) {}
    override fun historicalDataUpdate(p0: Int, p1: Bar?) {}
    override fun rerouteMktDataReq(p0: Int, p1: Int, p2: String?) {}
    override fun rerouteMktDepthReq(p0: Int, p1: Int, p2: String?) {}
    override fun marketRule(p0: Int, p1: Array<out PriceIncrement?>?) {}
    override fun pnl(p0: Int, p1: Double, p2: Double, p3: Double) {}
    override fun pnlSingle(p0: Int, p1: Decimal?, p2: Double, p3: Double, p4: Double, p5: Double) {}
    override fun historicalTicks(p0: Int, p1: List<HistoricalTick?>?, p2: Boolean) {}
    override fun historicalTicksBidAsk(p0: Int, p1: List<HistoricalTickBidAsk?>?, p2: Boolean) {}
    override fun historicalTicksLast(p0: Int, p1: List<HistoricalTickLast?>?, p2: Boolean) {}
    override fun tickByTickAllLast(p0: Int, p1: Int, p2: Long, p3: Double, p4: Decimal?, p5: TickAttribLast?, p6: String?, p7: String?) {}
    override fun tickByTickBidAsk(p0: Int, p1: Long, p2: Double, p3: Double, p4: Decimal?, p5: Decimal?, p6: TickAttribBidAsk?) {}
    override fun tickByTickMidPoint(p0: Int, p1: Long, p2: Double) {}
    override fun orderBound(p0: Long, p1: Int, p2: Int) {}
    override fun completedOrder(p0: Contract?, p1: Order?, p2: OrderState?) {}
    override fun completedOrdersEnd() {}
    override fun replaceFAEnd(p0: Int, p1: String?) {}
    override fun wshMetaData(p0: Int, p1: String?) {}
    override fun wshEventData(p0: Int, p1: String?) {}
    override fun historicalSchedule(p0: Int, p1: String?, p2: String?, p3: String?, p4: List<HistoricalSession?>?) {}
    override fun userInfo(p0: Int, p1: String?) {}
    override fun currentTimeInMillis(p0: Long) {}

    //===========================================================//
    //===========================================================//



    override fun connectionClosed() {
        logger.info("IBKR connection closed")
    }

    override fun connectAck() {
        logger.info("IBKR connection acknowledged")
    }

    override fun orderStatusProtoBuf(
        message: OrderStatusProto.OrderStatus?
    ) {
        if(message == null) {
            logger.warn("Received null IBKR orderStatus protobuf message")
            return
        }

        when(message.status){
            "Submitted" -> {
                logger.info(
                    "IBKR order submitted successfully. orderId={}, filled={}, remaining={}",
                    message.orderId,
                    message.filled,
                    message.remaining
                )
                event.publishEvent(OrderSubmittedEvent(message.orderId))
            }
            "Filled" -> {
                logger.info(
                    "IBKR order filled. orderId={}, filledQuantity={}, averageFillPrice={}",
                    message.orderId,
                    message.filled,
                    message.avgFillPrice
                )
                event.publishEvent(OrderFilledEvent(message.orderId, message.filled, message.avgFillPrice))
            }
            "Cancelled" -> {
                logger.warn(
                    "IBKR order cancelled. orderId={}, filledQuantity={}, remaining={}",
                    message.orderId,
                    message.filled,
                    message.remaining
                )
                event.publishEvent(OrderCancelledEvent(message.orderId))
            }
        }
    }

    override fun openOrderProtoBuf(
        message: OpenOrderProto.OpenOrder?
    ) {
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

    override fun openOrdersEndProtoBuf(
        message: OpenOrdersEndProto.OpenOrdersEnd?
    ) {
        logger.debug("IBKR open-order snapshot completed")
    }

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

    override fun completedOrderProtoBuf(p0: CompletedOrderProto.CompletedOrder?) {}

    override fun completedOrdersEndProtoBuf(p0: CompletedOrdersEndProto.CompletedOrdersEnd?) {}

    override fun orderBoundProtoBuf(p0: OrderBoundProto.OrderBound?) {}

    override fun contractDataProtoBuf(p0: ContractDataProto.ContractData?) {}

    override fun bondContractDataProtoBuf(p0: ContractDataProto.ContractData?) {}

    override fun contractDataEndProtoBuf(p0: ContractDataEndProto.ContractDataEnd?) {}

    override fun tickPriceProtoBuf(message: TickPriceProto.TickPrice?) {
        if(message == null || message.price <= 0) return

        if(message.tickType != 4 && message.tickType != 68) return

        pendingPrices[message.reqId]
            ?.takeIf { !it.isCompleted }
            ?.complete(message.price)
    }

    override fun tickSizeProtoBuf(p0: TickSizeProto.TickSize?) {}

    override fun tickOptionComputationProtoBuf(p0: TickOptionComputationProto.TickOptionComputation?) {}

    override fun tickGenericProtoBuf(p0: TickGenericProto.TickGeneric?) {}

    override fun tickStringProtoBuf(p0: TickStringProto.TickString?) {}

    override fun tickSnapshotEndProtoBuf(p0: TickSnapshotEndProto.TickSnapshotEnd?) {}

    override fun updateMarketDepthProtoBuf(p0: MarketDepthProto.MarketDepth?) {}

    override fun updateMarketDepthL2ProtoBuf(p0: MarketDepthL2Proto.MarketDepthL2?) {}

    override fun marketDataTypeProtoBuf(p0: MarketDataTypeProto.MarketDataType?) {}

    override fun tickReqParamsProtoBuf(p0: TickReqParamsProto.TickReqParams?) {}

    override fun updateAccountValueProtoBuf(p0: AccountValueProto.AccountValue?) {}

    override fun updatePortfolioProtoBuf(p0: PortfolioValueProto.PortfolioValue?) {}

    override fun updateAccountTimeProtoBuf(p0: AccountUpdateTimeProto.AccountUpdateTime?) {}

    override fun accountDataEndProtoBuf(p0: AccountDataEndProto.AccountDataEnd?) {}

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

    override fun positionProtoBuf(p0: PositionProto.Position?) {}

    override fun positionEndProtoBuf(p0: PositionEndProto.PositionEnd?) {}

    override fun accountSummaryProtoBuf(message: AccountSummaryProto.AccountSummary?) {
        if(message == null) return
        if(message.tag != "AvailableFunds") return
        val value = message.value.toDoubleOrNull()?: return
        pendingAccountSummaryRequests[message.reqId]
            ?.takeIf{pending-> !pending.isCompleted}
            ?.complete(value)
    }

    override fun accountSummaryEndProtoBuf(p0: AccountSummaryEndProto.AccountSummaryEnd?) {
        logger.debug("IBKR account summary snapshot completed")
    }

    override fun positionMultiProtoBuf(p0: PositionMultiProto.PositionMulti?) {}

    override fun positionMultiEndProtoBuf(p0: PositionMultiEndProto.PositionMultiEnd?) {}

    override fun accountUpdateMultiProtoBuf(p0: AccountUpdateMultiProto.AccountUpdateMulti?) {}

    override fun accountUpdateMultiEndProtoBuf(p0: AccountUpdateMultiEndProto.AccountUpdateMultiEnd?) {}

    override fun historicalDataProtoBuf(message: HistoricalDataProto.HistoricalData?) {
        if(message == null) return
        val request = pendingHistoricalData[message.reqId]?:return

        message.historicalDataBarsList.forEach {bar ->
            request.bars.add(
                IbkrHistoricalBar(
                    date = bar.date,
                    closingPrice = bar.close
                )
            )
        }
    }

    override fun historicalDataUpdateProtoBuf(p0: HistoricalDataUpdateProto.HistoricalDataUpdate?) {}

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

    override fun realTimeBarTickProtoBuf(p0: RealTimeBarTickProto.RealTimeBarTick?) {}

    override fun headTimestampProtoBuf(p0: HeadTimestampProto.HeadTimestamp?) {}

    override fun histogramDataProtoBuf(p0: HistogramDataProto.HistogramData?) {}

    override fun historicalTicksProtoBuf(p0: HistoricalTicksProto.HistoricalTicks?) {}

    override fun historicalTicksBidAskProtoBuf(p0: HistoricalTicksBidAskProto.HistoricalTicksBidAsk?) {}

    override fun historicalTicksLastProtoBuf(p0: HistoricalTicksLastProto.HistoricalTicksLast?) {}

    override fun tickByTickDataProtoBuf(p0: TickByTickDataProto.TickByTickData?) {}

    override fun updateNewsBulletinProtoBuf(p0: NewsBulletinProto.NewsBulletin?) {}

    override fun newsArticleProtoBuf(p0: NewsArticleProto.NewsArticle?) {}

    override fun newsProvidersProtoBuf(p0: NewsProvidersProto.NewsProviders?) {}

    override fun historicalNewsProtoBuf(p0: HistoricalNewsProto.HistoricalNews?) {}

    override fun historicalNewsEndProtoBuf(p0: HistoricalNewsEndProto.HistoricalNewsEnd?) {}

    override fun wshMetaDataProtoBuf(p0: WshMetaDataProto.WshMetaData?) {}

    override fun wshEventDataProtoBuf(p0: WshEventDataProto.WshEventData?) {}

    override fun tickNewsProtoBuf(p0: TickNewsProto.TickNews?) {}

    override fun scannerParametersProtoBuf(p0: ScannerParametersProto.ScannerParameters?) {}

    override fun scannerDataProtoBuf(p0: ScannerDataProto.ScannerData?) {}

    override fun fundamentalsDataProtoBuf(p0: FundamentalsDataProto.FundamentalsData?) {}

    override fun pnlProtoBuf(p0: PnLProto.PnL?) {}

    override fun pnlSingleProtoBuf(p0: PnLSingleProto.PnLSingle?) {}

    override fun receiveFAProtoBuf(p0: ReceiveFAProto.ReceiveFA?) {}

    override fun replaceFAEndProtoBuf(p0: ReplaceFAEndProto.ReplaceFAEnd?) {}

    override fun commissionAndFeesReportProtoBuf(p0: CommissionAndFeesReportProto.CommissionAndFeesReport?) {}

    override fun historicalScheduleProtoBuf(p0: HistoricalScheduleProto.HistoricalSchedule?) {}

    override fun rerouteMarketDataRequestProtoBuf(p0: RerouteMarketDataRequestProto.RerouteMarketDataRequest?) {}

    override fun rerouteMarketDepthRequestProtoBuf(p0: RerouteMarketDepthRequestProto.RerouteMarketDepthRequest?) {}

    override fun secDefOptParameterProtoBuf(p0: SecDefOptParameterProto.SecDefOptParameter?) {}

    override fun secDefOptParameterEndProtoBuf(p0: SecDefOptParameterEndProto.SecDefOptParameterEnd?) {}

    override fun softDollarTiersProtoBuf(p0: SoftDollarTiersProto.SoftDollarTiers?) {}

    override fun familyCodesProtoBuf(p0: FamilyCodesProto.FamilyCodes?) {}

    override fun symbolSamplesProtoBuf(p0: SymbolSamplesProto.SymbolSamples?) {}

    override fun smartComponentsProtoBuf(p0: SmartComponentsProto.SmartComponents?) {}

    override fun marketRuleProtoBuf(p0: MarketRuleProto.MarketRule?) {}

    override fun userInfoProtoBuf(p0: UserInfoProto.UserInfo?) {}

    override fun nextValidIdProtoBuf(message: NextValidIdProto.NextValidId?) {
        if(message == null) {
            logger.warn("Received null IBKR nextValidId protobuf message")
            return
        }

        nextOrderId.set(message.orderId)

        logger.info("IBKR next valid orderId={}", message.orderId)

        if(!readySignal.isCompleted) {
            readySignal.complete(Unit)
        }
    }

    override fun currentTimeProtoBuf(p0: CurrentTimeProto.CurrentTime?) {}

    override fun currentTimeInMillisProtoBuf(p0: CurrentTimeInMillisProto.CurrentTimeInMillis?) {}

    override fun verifyMessageApiProtoBuf(p0: VerifyMessageApiProto.VerifyMessageApi?) {}

    override fun verifyCompletedProtoBuf(p0: VerifyCompletedProto.VerifyCompleted?) {}

    override fun displayGroupListProtoBuf(p0: DisplayGroupListProto.DisplayGroupList?) {}

    override fun displayGroupUpdatedProtoBuf(p0: DisplayGroupUpdatedProto.DisplayGroupUpdated?) {}

    override fun marketDepthExchangesProtoBuf(p0: MarketDepthExchangesProto.MarketDepthExchanges?) {}

    override fun configResponseProtoBuf(p0: ConfigResponseProto.ConfigResponse?) {}

    override fun updateConfigResponseProtoBuf(p0: UpdateConfigResponseProto.UpdateConfigResponse?) {}
}