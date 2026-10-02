package application.service.broker

import application.logging.logger
import application.model.InteractiveBrokersOrder
import com.ib.client.Contract
import com.ib.client.Decimal
import com.ib.client.Order
import domain.market.security.SecurityIdentifier
import infrastructure.broker.IbkrHistoricalBar
import infrastructure.broker.IbkrPortfolioAccountSummary
import infrastructure.broker.InteractiveBrokerSessionManager
import kotlinx.coroutines.delay
import org.springframework.stereotype.Service
import java.util.UUID
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Instant

@Service
class InteractiveBrokersService(
    private val session: InteractiveBrokerSessionManager,
) {
    //===========================================================//
    //===========================================================//
    // Private Field(s)

    private val logger = logger<InteractiveBrokersService>()

    //===========================================================//
    //===========================================================//
    // Public Method(s)

    suspend fun placeOrder(userId: UUID, order: InteractiveBrokersOrder) {
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

    suspend fun getAccountSummary(userId: UUID): IbkrPortfolioAccountSummary{
        val client = session.getClient(userId)
        return client.getAccountSummary()
    }

    //===========================================================//

    suspend fun getHistoricalData(
        userId: UUID,
        securityIdentifier: SecurityIdentifier,
        from: Instant,
        to: Instant
    ): List<IbkrHistoricalBar> {
        val client = session.getClient(userId)

        val allBars =
            mutableListOf<IbkrHistoricalBar>()

        var currentEnd = to

        while (currentEnd > from) {

            val currentStart =
                maxOf(
                    from,
                    currentEnd - 7.days
                )

            logger.info(
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

    suspend fun reserveOrderId(userId: UUID): Int {
        val client = session.getClient(userId)
        return client.reserveOrderId()
    }

    //===========================================================//
    //===========================================================//
    // Private Method(s)

    private suspend fun createStockContract(order: InteractiveBrokersOrder): Contract {
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
}