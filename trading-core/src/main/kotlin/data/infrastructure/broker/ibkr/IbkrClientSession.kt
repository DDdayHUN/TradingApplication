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
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Instant

class IbkrClientSession : IbkrInterfaceWrapper {
    //===========================================================//
    //===========================================================//
    // Public Field(s)

    val userId: UUID
    val clientId: Int

    //===========================================================//
    //===========================================================//
    // Private Field(s)

    private val m_Host: String
    private val m_Port: Int
    private val m_EventPublisher: ApplicationEventPublisher

    private val m_Signal = EJavaSignal()
    private val client = EClientSocket(this, m_Signal)
    private var reader: EReader? = null
    private var readerThread: Thread? = null
    private val nextOrderId = AtomicInteger(-1)
    private var readySignal = CompletableDeferred<Unit>()
    private val marketDataRequestId = AtomicInteger(1_000)
    private val pendingPrices = ConcurrentHashMap<Int, CompletableDeferred<Double>>()

    private val accountSummaryRequestId = 2_000

    @Volatile
    private var latestAvailableFunds: Double? = null

    @Volatile
    private var latestNetLiquidation: Double? = null

    private val historicalDataRequestId = AtomicInteger(3_000)
    private val pendingHistoricalData = ConcurrentHashMap<Int, HistoricalDataRequest>()

    private val historicalEndDateFormatter = DateTimeFormatter.ofPattern("yyyyMMdd HH:mm:ss 'UTC'").withZone(ZoneOffset.UTC)!!

    //===========================================================//

    private val m_Logger = logger<IbkrClientSession>()

    //===========================================================//
    //===========================================================//
    // Public Method(s)

    suspend fun connect(maxAttempts: Int = 5) {
        if(client.isConnected && nextOrderId.get() >= 0) {
            m_Logger.warn("IBKR client is already connected and ready")
            return
        }

        repeat(maxAttempts) { attempt ->
            try{
                m_Logger.info("Connecting to IBKR attemp={}/{} host={}, port={} clientId={}",
                    attempt + 1, maxAttempts, m_Host, m_Port, clientId)

                nextOrderId.set(-1)
                readySignal = CompletableDeferred()

                if(client.isConnected) {
                    client.eDisconnect()
                }

                client.eConnect(m_Host, m_Port, clientId)

                if(!client.isConnected) {
                    throw IllegalStateException(
                        "Could not connect to IBKR Gateway"
                    )
                }

                startMessageReader()

                withTimeout(10_000.milliseconds) {
                    readySignal.await()
                }

                m_Logger.info("IBKR client is ready")

                subscribeAccountSummary()
                return
            } catch(e: Exception) {
                m_Logger.warn("IBKR conntection attempt {}/{} failed",
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
            m_Logger.debug("IBKR client is already disconnected")
            return
        }

        m_Logger.info("Disconnecting from IBKR Gateway")
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

        m_Logger.info(
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
        m_Logger.debug("Requesting current IBKR open orders")
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
    // Ibkr Method(s)

    override fun connectionClosed() = m_Logger.info("IBKR connection closed")
    override fun connectAck() = m_Logger.info("IBKR connection acknowledged")

    //===========================================================//

    override fun orderStatusProtoBuf(message: OrderStatusProto.OrderStatus?) {
        if(message == null) {
            m_Logger.warn("Received null IBKR orderStatus protobuf message")
            return
        }

        when(message.status) {
            "ApiPending", "PendingSubmit" -> {
                m_EventPublisher.publishEvent(IbkrEvent.OrderPendingEvent(userId, message.orderId))
            }
            "PreSubmitted" -> {
                m_EventPublisher.publishEvent(IbkrEvent.OrderPreSubmittedEvent(userId, message.orderId))
            }
            "Submitted" -> {
                m_EventPublisher.publishEvent(IbkrEvent.OrderSubmittedEvent(userId, message.orderId))
            }
            "PendingCancel" -> {
                m_EventPublisher.publishEvent(IbkrEvent.OrderPendingCancelEvent(userId, message.orderId))
            }
            "Filled" -> {
                m_EventPublisher.publishEvent(IbkrEvent.OrderFilledEvent(userId, message.orderId, message.filled, message.avgFillPrice))
            }
            "ApiCancelled", "Cancelled" -> {
                m_EventPublisher.publishEvent(IbkrEvent.OrderCancelledEvent(userId, message.orderId))
            }
            "Inactive" -> {
                m_EventPublisher.publishEvent(IbkrEvent.OrderInactiveEvent(userId, message.orderId))
            }
        }
    }

    //===========================================================//

    override fun openOrderProtoBuf(message: OpenOrderProto.OpenOrder?) {
        if(message == null) {
            m_Logger.warn("Received null IBKR openOrder protobuf message")
            return
        }

        m_Logger.debug(
            "IBKR open order orderId={} symbol={} action={} status={}",
            message.orderId,
            message.contract.symbol,
            message.order.action,
            message.orderState.status
        )
    }

    //===========================================================//

    override fun openOrdersEndProtoBuf(message: OpenOrdersEndProto.OpenOrdersEnd?) {
        m_Logger.debug("IBKR open-order snapshot completed")
    }

    //===========================================================//

    override fun errorProtoBuf(message: ErrorMessageProto.ErrorMessage?) {
        if(message == null) {
            m_Logger.warn("Received null IBKR protobuf error message")
            return
        }

        when(message.errorCode) {
            2104, 2106, 2158 -> m_Logger.info(
                "IBKR status requestId={} code={} message={}",
                message.id,
                message.errorCode,
                message.errorMsg
            )

            2107, 2108 -> m_Logger.debug(
                "IBKR status requestId={} code={} message={}",
                message.id,
                message.errorCode,
                message.errorMsg
            )

            399 -> m_Logger.warn(
                "IBKR order warning requestId={} code={} message={}",
                message.id,
                message.errorCode,
                message.errorMsg
            )

            else -> m_Logger.error(
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
            m_Logger.warn("Received null IBKR executionDetails protobuf message")
            return
        }

        m_Logger.info(
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
            m_Logger.warn("Received null IBKR executionDetailsEnd protobuf message")
            return
        }

        m_Logger.debug(
            "IBKR execution details completed reqId={}",
            message.reqId
        )
    }

    //===========================================================//

    override fun tickPriceProtoBuf(message: TickPriceProto.TickPrice?) {
        if (message == null || message.price <= 0) return

        m_Logger.info(
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

    //===========================================================//

    override fun managedAccountsProtoBuf(message: ManagedAccountsProto.ManagedAccounts?) {
        if(message == null) {
            m_Logger.warn("Received null IBKR managedAccounts protobuf message")
            return
        }

        m_Logger.info(
            "IBKR managed accounts account={}",
            message.accountsList
        )
    }

    //===========================================================//

    override fun accountSummaryProtoBuf(message: AccountSummaryProto.AccountSummary?) {
        if (message == null) return

        val value = message.value.toDoubleOrNull()
            ?: return

        when (message.tag) {

            "AvailableFunds" -> {
                latestAvailableFunds = value

                m_Logger.debug(
                    "IBKR AvailableFunds updated: {}",
                    value
                )
            }

            "NetLiquidation" -> {
                latestNetLiquidation = value

                m_Logger.debug(
                    "IBKR NetLiquidation updated: {}",
                    value
                )
            }
        }
    }

    //===========================================================//

    override fun accountSummaryEndProtoBuf(p0: AccountSummaryEndProto.AccountSummaryEnd?) {
        m_Logger.debug("IBKR account summary snapshot completed")
    }

    //===========================================================//

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

    //===========================================================//

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

    //===========================================================//

    override fun nextValidIdProtoBuf(message: NextValidIdProto.NextValidId?) {
        if(message == null) {
            m_Logger.warn("Received null IBKR nextValidId protobuf message")
            return
        }

        nextOrderId.updateAndGet { current ->
            maxOf(current, message.orderId)
        }

        m_Logger.info("IBKR next valid orderId={}", message.orderId)

        if(!readySignal.isCompleted) {
            readySignal.complete(Unit)
        }
    }

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

        m_Logger.info("Subscribed to IBKR account summary")
    }

    //===========================================================//

    private fun startMessageReader() {
        m_Logger.debug("Starting IBKR message reader")

        reader = EReader(client, m_Signal)

        reader?.start()

        readerThread = Thread {
            while(client.isConnected){
                try{
                    m_Signal.waitForSignal()
                    reader?.processMsgs()
                } catch(e: InterruptedException){
                    Thread.currentThread().interrupt()
                    m_Logger.debug("IBKR reader thread interrupted")
                    break
                } catch(e: Exception){
                    m_Logger.error("Unexpected error in IBKR reader thread", e)
                }
            }
        }.apply {
            name = "ibkr-reader"
            isDaemon = true
            start()
        }
    }

    //===========================================================//

    private fun formatHistoricalEndDate(to: Instant): String {
        val javaInstant = java.time.Instant.ofEpochMilli(
            to.toEpochMilliseconds()
        )

        return historicalEndDateFormatter.format(javaInstant)
    }

    //===========================================================//
    //===========================================================//
    // Constructor(s)

    constructor(userId: UUID, clientId: Int, host: String, port: Int, eventPublisher: ApplicationEventPublisher) {
        this.userId = userId
        this.clientId = clientId

        m_Host = host
        m_Port = port
        m_EventPublisher = eventPublisher
    }
}