package application.service.broker.ibkr

import application.logging.logger
import application.service.broker.IBrokerService
import application.service.user.User
import com.ib.client.Contract
import com.ib.client.Decimal
import com.ib.client.Order
import domain.trader.TradingOrder
import data.infrastructure.broker.ibkr.IbkrEvent
import data.infrastructure.broker.ibkr.IbkrHistoricalBar
import data.infrastructure.broker.ibkr.InteractiveBrokerSessionManager
import data.infrastructure.broker.ibkr.InteractiveBrokersOrder
import data.infrastructure.broker.ibkr.InteractiveBrokersOrder.Status
import data.infrastructure.broker.ibkr.order.sql.IInteractiveBrokersOrderRepository
import data.infrastructure.broker.ibkr.order.sql.toEntity
import data.infrastructure.broker.ibkr.toInteractiveBrokersOrder
import data.repository.order.ITradingOrderRepository
import data.repository.order.sql.toDomain
import data.repository.order.sql.toEntity
import data.repository.trader.ITraderRepository
import data.repository.user.IUserRepository
import domain.market.security.SecurityIdentifier
import kotlinx.coroutines.delay
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Instant

@Service
class InteractiveBrokersService(
    private val sessionManager: InteractiveBrokerSessionManager,
    private val userRepository: IUserRepository,
    private val traderRepository: ITraderRepository,
    private val tradingOrderRepository: ITradingOrderRepository,
    private val ibkrOrderRepository: IInteractiveBrokersOrderRepository
) : IBrokerService {
    // TODO : This class needs to be frissitve, cuz this is not good currently.
    //===========================================================//
    //===========================================================//
    // Private Field(s)

    private val m_Logger = logger<InteractiveBrokersService>()

    //===========================================================//
    //===========================================================//
    // Public Method(s)

    @Transactional
    override suspend fun submitOrderForUser(userId: UUID, order: TradingOrder) {
        val userEntity = userRepository.getById(userId).getOrThrow()
        val traderEntity = traderRepository.getById(order.traderId).getOrThrow()

        if(!userEntity.portfolio.traders.contains(traderEntity)) {
            throw IllegalArgumentException("Trader not found for user with ID:{${userEntity.id}}")
        }


        val orderEntity = order.toEntity(traderEntity)
        val ibkrOrder = order.toInteractiveBrokersOrder(reserveOrderId(userId))
        ibkrOrder.toEntity(orderEntity)

        tradingOrderRepository.save().getOrThrow()
        ibkrOrderRepository.save(ibkrOrder).getOrThrow()



        try {
            placeOrder(userId, ibkrOrder)
        }
        catch(e: Exception) {
            ibkrOrderRepository.save(ibkrOrder.copy(
                status = Status.CANCELLED
            )).getOrThrow()
            throw e
        }
    }

    //===========================================================//

    @Transactional(readOnly = true)
    override suspend fun getAllOrderForUser(userId: UUID): List<TradingOrder> {
        return ibkrOrderRepository.getAllByUserId(userId).getOrThrow().map { order -> order.tradingOrder.toDomain() }
    }

    //===========================================================//

    override suspend fun getAccountSummary(userId: UUID): User.AccountSummary {
        val client = sessionManager.getClient(userId)
        val ibkrAccSum = client.getAccountSummary()

        return User.AccountSummary(
            availableCapital = ibkrAccSum.availableCapital,
            netLiquidation = ibkrAccSum.netLiquidation
        )
    }

    //===========================================================//

    suspend fun getHistoricalData(userId: UUID, securityIdentifier: SecurityIdentifier, from: Instant, to: Instant): List<IbkrHistoricalBar> {
        val client = sessionManager.getClient(userId)

        val allBars = mutableListOf<IbkrHistoricalBar>()

        var currentEnd = to

        while (currentEnd > from) {

            val currentStart =
                maxOf(
                    from,
                    currentEnd - 7.days
                )

            m_Logger.info(
                "Fetching IBKR historical data ticker={} from={} to={}",
                securityIdentifier.tickerSymbol,
                currentStart,
                currentEnd
            )

            val bars =
                client.getHistoricalData(
                    identifier = securityIdentifier,
                    from = currentStart,
                    to = currentEnd
                )

            allBars.addAll(bars)

            currentEnd = currentStart

            delay(500.milliseconds)
        }

        return allBars
            .distinctBy { it.timestamp }
            .sortedBy { it.timestamp }
    }

    //===========================================================//
    //===========================================================//
    // Event Listener(s)

    @EventListener
    @Transactional
    suspend fun handle(event: IbkrEvent.OrderSubmittedEvent) {
        val order = ibkrOrderRepository.getById(event.orderId).getOrThrow()

        if (order.status == Status.FILLED       ||
            order.status == Status.CANCELLED    ||
            order.status == Status.SUBMITTED
        ) return

        order.status = Status.SUBMITTED
        ibkrOrderRepository.save(order).getOrThrow()

        m_Logger.info("IBKR order submitted with ID:{${order.id}}")
    }

    //===========================================================//

    @EventListener
    @Transactional
    suspend fun handle(event: IbkrEvent.OrderCancelledEvent) {
        val order = ibkrOrderRepository.getById(event.orderId).getOrThrow()

        if(order.status == Status.FILLED) return

        order.status = Status.CANCELLED
        ibkrOrderRepository.save(order).getOrThrow()

        m_Logger.info("IBKR order cancelled with ID:{${order.id}}")
    }

    //===========================================================//

    @EventListener
    @Transactional
    suspend fun handle(event: IbkrEvent.OrderFilledEvent) {
        val order = ibkrOrderRepository.getById(event.orderId).getOrThrow()
        if(order.status == Status.FILLED) return

        when (val signal = order.signal) {

            is InteractiveBrokersOrder.Signal.Buy -> {
                traderService.applyBuyFill(
                    traderId = order.traderId,
                    filledQuantity = event.filled.toInt(),
                    averageFillPrice = event.averageFillPrice
                )
            }

            is InteractiveBrokersOrder.Signal.Sell -> {
                traderService.applySellFill(
                    traderId = order.traderId,
                    sellAllocations = signal.allocations,
                    averageFillPrice = event.averageFillPrice
                )
            }
        }

        m_Logger.info(
            "Applying filled order orderId={} quantity={} avgPrice={}",
            event.orderId,
            event.filled,
            event.averageFillPrice
        )

        val filledOrder = order.fill(
            filledQuantity = event.filled,
            averageFillPrice = event.averageFillPrice
        ).getOrThrow()

        ibkrOrderRepository.save(filledOrder).getOrThrow()

        if (order.signal is InteractiveBrokersOrder.Signal.Sell) {
            ibkrOrderRepository
                .clearOrderAllocation(filledOrder.id)
                .getOrThrow()
        }
    }

    //===========================================================//

    @EventListener
    @Transactional
    suspend fun handle(event: IbkrEvent.OrderPreSubmittedEvent) {
        val order = ibkrOrderRepository.getById(event.orderId).getOrThrow()

        if(order.status != Status.PENDING) return

        order.status = Status.PRE_SUBMITTED
        ibkrOrderRepository.save(order).getOrThrow()

        m_Logger.info("IBKR order pre-submitted with ID:{${order.id}}")
    }

    //===========================================================//

    @EventListener
    @Transactional(readOnly = true)
    suspend fun handle(event: IbkrEvent.OrderPendingEvent) {
        val order = ibkrOrderRepository.getById(event.orderId).getOrThrow()

        if(order.status != Status.PENDING) return

        m_Logger.info("IBKR order pending with ID:{${order.id}}")
    }

    //===========================================================//

    @EventListener
    @Transactional
    suspend fun handle(event: IbkrEvent.OrderPendingCancelEvent) {
        val order = ibkrOrderRepository.getById(event.orderId).getOrThrow()

        if (order.status == Status.FILLED           ||
            order.status == Status.CANCELLED        ||
            order.status == Status.PENDING_CANCEL
        ) return

        order.status = Status.PENDING_CANCEL

        ibkrOrderRepository.save(order).getOrThrow()

        m_Logger.info("IBKR order pending cancellation with ID:{${order.id}}")
    }

    //===========================================================//

    @EventListener
    @Transactional
    suspend fun handle(event: IbkrEvent.OrderInactiveEvent) {
        val order = ibkrOrderRepository.getById(event.orderId).getOrThrow()

        if (order.status == Status.FILLED       ||
            order.status == Status.CANCELLED    ||
            order.status == Status.INACTIVE
        ) return

        order.status = Status.INACTIVE

        ibkrOrderRepository.save(order).getOrThrow()

        m_Logger.warn("IBKR order inactive with ID:{${order.id}}")
    }

    //===========================================================//
    //===========================================================//
    // Private Method(s)

    private suspend fun placeOrder(userId: UUID, order: InteractiveBrokersOrder) {
        val client = sessionManager.getClient(userId)
        val clientContract = createStockContract(order)
        val clientOrder = createMarketOrder(order)

        client.placeOrder(
            orderId = order.brokerOrderId,
            contract = clientContract,
            order = clientOrder
        )
    }

    //===========================================================//

    private fun createStockContract(order: InteractiveBrokersOrder): Contract {
        val securityIdentifier = order.tradingOrder.securityIdentifier
        return Contract().apply {
            symbol(securityIdentifier.tickerSymbol)
            secType("STK")
            exchange("SMART")
            currency(securityIdentifier.currency)
        }
    }

    //===========================================================//

    private fun createMarketOrder(order: InteractiveBrokersOrder): Order {
        val tradingOrderSignal = order.tradingOrder.signal
        val action = if(tradingOrderSignal is TradingOrder.Signal.Buy) "BUY" else "SELL"
        val quantity = when (tradingOrderSignal) {
            is TradingOrder.Signal.Buy -> tradingOrderSignal.amount.toDouble()
            is TradingOrder.Signal.Sell -> tradingOrderSignal.sellHoldings.sumOf { (_, amount) -> amount }.toDouble()
        }

        return Order().apply {
            action(action)
            orderType("MKT")
            totalQuantity(Decimal.get(quantity))
            tif("DAY")
        }
    }

    //===========================================================//

    private fun reserveOrderId(userId: UUID): Int {
        val client = sessionManager.getClient(userId)
        return client.reserveOrderId()
    }
}