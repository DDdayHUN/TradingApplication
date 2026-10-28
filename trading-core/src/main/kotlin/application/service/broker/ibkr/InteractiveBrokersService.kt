package application.service.broker.ibkr

import application.logging.logger
import application.service.broker.IBrokerService
import application.service.trader.ITraderService
import application.service.user.User
import com.ib.client.Contract
import com.ib.client.Decimal
import com.ib.client.Order
import data.repository.order.sql.InteractiveBrokersOrderRepository
import domain.trader.TradingOrder
import data.infrastructure.broker.ibkr.IbkrEvent
import data.infrastructure.broker.ibkr.IbkrHistoricalBar
import data.infrastructure.broker.ibkr.InteractiveBrokerSessionManager
import domain.market.security.SecurityIdentifier
import kotlinx.coroutines.delay
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Service
import java.util.*
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Instant

@Service
class InteractiveBrokersService(
    private val session: InteractiveBrokerSessionManager,
    private val traderService: ITraderService,
    private val orderRepository: InteractiveBrokersOrderRepository
) : IBrokerService {
    //===========================================================//
    //===========================================================//
    // Private Field(s)

    private val m_Logger = logger<InteractiveBrokersService>()

    //===========================================================//
    //===========================================================//
    // Public Method(s)

    override suspend fun submitOrder(userId: UUID, order: TradingOrder) {
        val persistedOrders = order.toInteractiveBrokersOrder(reserveOrderId(userId))

        persistedOrders.forEach { order ->
            orderRepository.save(order).getOrThrow()
        }

        persistedOrders.forEach { order ->
            try {
                placeOrder(userId,order)
            } catch(e: Exception) {
                orderRepository.save(order.copy(
                    status = InteractiveBrokersOrder.Status.CANCELLED
                )).getOrThrow()
                throw e
            }
        }
    }

    //===========================================================//

    override suspend fun getAllOrder(userId: UUID): List<TradingOrder> {
        return orderRepository.getAll(userId).getOrThrow().map { order -> order.toDomain() }
    }

    //===========================================================//

    override fun getAccountSummary(userId: UUID): User.AccountSummary {
        val client = session.getClient(userId)
        val ibkrAccSum = client.getAccountSummary()

        return User.AccountSummary(
            availableCapital = ibkrAccSum.availableCapital,
            netLiquidation = ibkrAccSum.netLiquidation
        )
    }

    //===========================================================//


    suspend fun getHistoricalData(userId: UUID, securityIdentifier: SecurityIdentifier, from: Instant, to: Instant): List<IbkrHistoricalBar> {
        val client = session.getClient(userId)

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
    suspend fun handle(event: IbkrEvent.OrderSubmittedEvent) {
        val order = orderRepository.getByIbkrOrderId(event.orderId).getOrThrow()
        if (order.status == InteractiveBrokersOrder.Status.FILLED ||
            order.status == InteractiveBrokersOrder.Status.CANCELLED ||
            order.status == InteractiveBrokersOrder.Status.SUBMITTED
        ) return
        orderRepository.save(order.submit().getOrThrow()).getOrThrow()
    }

    //===========================================================//

    @EventListener
    suspend fun handle(event: IbkrEvent.OrderCancelledEvent) {
        val order = orderRepository.getByIbkrOrderId(event.orderId).getOrThrow()
        if(order.status == InteractiveBrokersOrder.Status.FILLED) return

        orderRepository.save(order.cancel().getOrThrow()).getOrThrow()
    }

    //===========================================================//

    @EventListener
    suspend fun handle(event: IbkrEvent.OrderFilledEvent) {
        val order = orderRepository.getByIbkrOrderId(event.orderId).getOrThrow()
        if(order.status == InteractiveBrokersOrder.Status.FILLED) return

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

        orderRepository.save(filledOrder).getOrThrow()

        if (order.signal is InteractiveBrokersOrder.Signal.Sell) {
            orderRepository
                .clearOrderAllocation(filledOrder.id)
                .getOrThrow()
        }
    }

    //===========================================================//

    @EventListener
    suspend fun handle(event: IbkrEvent.OrderPreSubmittedEvent) {
        val order = orderRepository.getByIbkrOrderId(event.orderId).getOrThrow()
        if(order.status != InteractiveBrokersOrder.Status.PENDING) return

        orderRepository.save(
            order.copy(
                status = InteractiveBrokersOrder.Status.PRE_SUBMITTED
            )
        ).getOrThrow()

        m_Logger.info("IBKR order pre-submitted orderId=${event.orderId}")
    }

    //===========================================================//

    @EventListener
    suspend fun handle(event: IbkrEvent.OrderPendingEvent) {
        val order = orderRepository.getByIbkrOrderId(event.orderId).getOrThrow()
        if(order.status != InteractiveBrokersOrder.Status.PENDING) return

        m_Logger.info("IBKR order pending orderId=${event.orderId}")
    }

    //===========================================================//

    @EventListener
    suspend fun handle(event: IbkrEvent.OrderPendingCancelEvent) {
        val order = orderRepository.getByIbkrOrderId(event.orderId).getOrThrow()
        if (order.status == InteractiveBrokersOrder.Status.FILLED ||
            order.status == InteractiveBrokersOrder.Status.CANCELLED ||
            order.status == InteractiveBrokersOrder.Status.PENDING_CANCEL
        ) return

        orderRepository.save(
            order.copy(
                status = InteractiveBrokersOrder.Status.PENDING_CANCEL
            )
        ).getOrThrow()

        m_Logger.info("IBKR order pending cancellation orderId=${event.orderId}")
    }

    //===========================================================//

    @EventListener
    suspend fun handle(event: IbkrEvent.OrderInactiveEvent) {
        val order = orderRepository.getByIbkrOrderId(event.orderId).getOrThrow()
        if (order.status == InteractiveBrokersOrder.Status.FILLED ||
            order.status == InteractiveBrokersOrder.Status.CANCELLED ||
            order.status == InteractiveBrokersOrder.Status.INACTIVE
        ) return

        orderRepository.save(
            order.copy(
                status = InteractiveBrokersOrder.Status.INACTIVE
            )
        ).getOrThrow()

        m_Logger.warn("IBKR order inactive orderId=${event.orderId}")
    }

    //===========================================================//
    //===========================================================//
    // Private Method(s)

    private fun placeOrder(userId: UUID, order: InteractiveBrokersOrder) {
        val client = session.getClient(userId)
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
        return Contract().apply {
            symbol(order.securityIdentifier.tickerSymbol)
            secType("STK")
            exchange("SMART")
            currency(order.securityIdentifier.currency)
        }
    }

    //===========================================================//

    private fun createMarketOrder(order: InteractiveBrokersOrder): Order {
        return Order().apply {
            action(order.action.name)
            orderType("MKT")
            totalQuantity(Decimal.get(order.quantity))
            tif("DAY")
        }
    }

    //===========================================================//

    private fun reserveOrderId(userId: UUID): Int {
        val client = session.getClient(userId)
        return client.reserveOrderId()
    }
}