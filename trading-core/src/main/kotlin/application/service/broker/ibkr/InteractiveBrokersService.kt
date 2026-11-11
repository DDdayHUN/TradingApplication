package application.service.broker.ibkr

import application.logging.logger
import application.service.broker.IBrokerService
import application.service.user.User
import com.ib.client.Contract
import com.ib.client.Decimal
import com.ib.client.Order
import data.infrastructure.broker.ibkr.order.sql.InteractiveBrokersOrderRepository
import domain.trader.TradingOrder
import data.infrastructure.broker.ibkr.IbkrEvent
import data.infrastructure.broker.ibkr.IbkrHistoricalBar
import data.infrastructure.broker.ibkr.InteractiveBrokerSessionManager
import data.infrastructure.broker.ibkr.InteractiveBrokersOrder
import data.infrastructure.broker.ibkr.order.sql.toEntity
import data.infrastructure.broker.ibkr.toDomain
import data.infrastructure.broker.ibkr.toInteractiveBrokersOrder
import data.repository.order.ITradingOrderRepository
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
    private val session: InteractiveBrokerSessionManager,
    private val userRepository: IUserRepository,
    private val traderRepository: ITraderRepository,
    private val tradingOrderRepository: ITradingOrderRepository,
    private val ibkrOrderRepository: InteractiveBrokersOrderRepository
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
    override suspend fun submitOrder(userId: UUID, order: TradingOrder) {
        val userEntity = userRepository.getById(userId).getOrThrow()
        val traderEntity = traderRepository.getById(order.traderId).getOrThrow()

        if(userEntity.portfolio.traders.contains())



        val orderEntity = order.toEntity()
        val ibkrOrder = order.toInteractiveBrokersOrder(reserveOrderId(userId))
        ibkrOrder.toEntity(order)

        tradingOrderRepository.save().getOrThrow()
        ibkrOrderRepository.save(ibkrOrder).getOrThrow()



        try {
            placeOrder(userId, ibkrOrder)
        }
        catch(e: Exception) {
            ibkrOrderRepository.save(ibkrOrder.copy(
                status = InteractiveBrokersOrder.Status.CANCELLED
            )).getOrThrow()
            throw e
        }

        fun PortfolioEntity.update(portfolio: Portfolio) {
            require(this.id == portfolio.id) { "ID mismatch. ID1 {${this.id}} ID2 {${portfolio.id}}" }

            val domainTradersById = portfolio.traders.associateBy { it.id }

            // remove traders that no longer exist
            traders.removeIf { it.id !in domainTradersById }

            // update existing traders
            traders.forEach { existing ->
                domainTradersById[existing.id]?.let { domainTrader ->
                    existing.update(domainTrader)
                }
            }

            // add new traders
            val existingIds = traders.map { it.id }.toSet()

            portfolio.traders
                .filter { it.id !in existingIds }
                .forEach { domainTrader ->
                    traders.add(domainTrader.toEntity(this))
                }
        }
    }

    //===========================================================//

    @Transactional(readOnly = true)
    override suspend fun getAllOrder(userId: UUID): List<TradingOrder> {
        return ibkrOrderRepository.getAll(userId).getOrThrow().map { order -> order.toDomain() }
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
        val order = ibkrOrderRepository.getByIbkrOrderId(event.orderId).getOrThrow()
        if (order.status == InteractiveBrokersOrder.Status.FILLED ||
            order.status == InteractiveBrokersOrder.Status.CANCELLED ||
            order.status == InteractiveBrokersOrder.Status.SUBMITTED
        ) return
        ibkrOrderRepository.save(order.submit().getOrThrow()).getOrThrow()
    }

    //===========================================================//

    @EventListener
    suspend fun handle(event: IbkrEvent.OrderCancelledEvent) {
        val order = ibkrOrderRepository.getByIbkrOrderId(event.orderId).getOrThrow()
        if(order.status == InteractiveBrokersOrder.Status.FILLED) return

        ibkrOrderRepository.save(order.cancel().getOrThrow()).getOrThrow()
    }

    //===========================================================//

    @EventListener
    suspend fun handle(event: IbkrEvent.OrderFilledEvent) {
        val order = ibkrOrderRepository.getByIbkrOrderId(event.orderId).getOrThrow()
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

        ibkrOrderRepository.save(filledOrder).getOrThrow()

        if (order.signal is InteractiveBrokersOrder.Signal.Sell) {
            ibkrOrderRepository
                .clearOrderAllocation(filledOrder.id)
                .getOrThrow()
        }
    }

    //===========================================================//

    @EventListener
    suspend fun handle(event: IbkrEvent.OrderPreSubmittedEvent) {
        val order = ibkrOrderRepository.getByIbkrOrderId(event.orderId).getOrThrow()
        if(order.status != InteractiveBrokersOrder.Status.PENDING) return

        ibkrOrderRepository.save(
            order.copy(
                status = InteractiveBrokersOrder.Status.PRE_SUBMITTED
            )
        ).getOrThrow()

        m_Logger.info("IBKR order pre-submitted orderId=${event.orderId}")
    }

    //===========================================================//

    @EventListener
    suspend fun handle(event: IbkrEvent.OrderPendingEvent) {
        val order = ibkrOrderRepository.getByIbkrOrderId(event.orderId).getOrThrow()
        if(order.status != InteractiveBrokersOrder.Status.PENDING) return

        m_Logger.info("IBKR order pending orderId=${event.orderId}")
    }

    //===========================================================//

    @EventListener
    suspend fun handle(event: IbkrEvent.OrderPendingCancelEvent) {
        val order = ibkrOrderRepository.getByIbkrOrderId(event.orderId).getOrThrow()
        if (order.status == InteractiveBrokersOrder.Status.FILLED ||
            order.status == InteractiveBrokersOrder.Status.CANCELLED ||
            order.status == InteractiveBrokersOrder.Status.PENDING_CANCEL
        ) return

        ibkrOrderRepository.save(
            order.copy(
                status = InteractiveBrokersOrder.Status.PENDING_CANCEL
            )
        ).getOrThrow()

        m_Logger.info("IBKR order pending cancellation orderId=${event.orderId}")
    }

    //===========================================================//

    @EventListener
    suspend fun handle(event: IbkrEvent.OrderInactiveEvent) {
        val order = ibkrOrderRepository.getByIbkrOrderId(event.orderId).getOrThrow()
        if (order.status == InteractiveBrokersOrder.Status.FILLED ||
            order.status == InteractiveBrokersOrder.Status.CANCELLED ||
            order.status == InteractiveBrokersOrder.Status.INACTIVE
        ) return

        ibkrOrderRepository.save(
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