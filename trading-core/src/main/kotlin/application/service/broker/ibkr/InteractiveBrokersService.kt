package application.service.broker.ibkr

import application.logging.logger
import application.service.broker.IBrokerService
import application.service.user.User
import com.ib.client.Contract
import com.ib.client.Decimal
import com.ib.client.Order
import domain.trader.TradingOrder
import data.infrastructure.broker.ibkr.IbkrHistoricalBar
import data.infrastructure.broker.ibkr.InteractiveBrokerSessionManager
import data.infrastructure.broker.ibkr.InteractiveBrokersOrder
import data.infrastructure.broker.ibkr.InteractiveBrokersOrder.Status
import data.infrastructure.broker.ibkr.sql.order.IInteractiveBrokersOrderRepository
import data.infrastructure.broker.ibkr.sql.order.toEntity
import data.infrastructure.broker.ibkr.toInteractiveBrokersOrder
import data.repository.order.ITradingOrderRepository
import data.repository.order.sql.toDomain
import data.repository.order.sql.toEntity
import data.repository.trader.ITraderRepository
import data.repository.user.IUserRepository
import domain.market.security.SecurityIdentifier
import kotlinx.coroutines.delay
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

        if (userEntity.portfolio.traders.none { it.id == traderEntity.id }) {
            throw IllegalArgumentException("Trader not found for user with ID: ${userEntity.id}")
        }

        val ibkrOrder = order.toInteractiveBrokersOrder(reserveOrderId(userId))

        val orderEntity = tradingOrderRepository.save(order.toEntity(traderEntity)).getOrThrow()
        val ibkrOrderEntity = ibkrOrderRepository.save(ibkrOrder.toEntity(orderEntity)).getOrThrow()

        try {
            val client = sessionManager.getClient(userId)
            val clientContract = createStockContract(ibkrOrder)
            val clientOrder = createMarketOrder(ibkrOrder)

            client.placeOrder(
                orderId = ibkrOrder.brokerOrderId,
                contract = clientContract,
                order = clientOrder
            )
        }
        catch(e: Exception) {
            ibkrOrderEntity.status = Status.CANCELLED
            ibkrOrderRepository.save(ibkrOrderEntity).getOrThrow()
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
    // Private Method(s)

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

    private suspend fun reserveOrderId(userId: UUID): Int {
        val client = sessionManager.getClient(userId)
        return client.reserveOrderId()
    }
}