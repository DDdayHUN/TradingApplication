package application.service.broker

import application.logging.logger
import application.service.trader.ITraderService
import com.ib.client.Contract
import com.ib.client.Decimal
import com.ib.client.Order
import data.repository.order.sql.InteractiveBrokersOrderRepository
import data.repository.trader.ITraderRepository
import data.repository.trader.sql.TraderRepository
import domain.market.security.SecurityIdentifier
import infrastructure.broker.IbkrAccountSummary
import infrastructure.broker.IbkrHistoricalBar
import infrastructure.broker.InteractiveBrokersSession
import kotlinx.coroutines.delay
import org.springframework.stereotype.Service
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Instant

@Service
class InteractiveBrokersService(
    private val session: InteractiveBrokersSession,
) {
    //===========================================================//
    //===========================================================//
    // Private Field(s)

    private val logger = logger<InteractiveBrokersService>()

    //===========================================================//
    //===========================================================//
    // Public Method(s)

    suspend fun placeOrder(order: InteractiveBrokersOrder) {
        val client = session.getClient()
        val clientContract = createStockContract(order)
        val clientOrder = createMarketOrder(order)

        client.placeOrder(
            orderId = order.brokerOrderId,
            contract = clientContract,
            order = clientOrder
        )
    }

    //===========================================================//

    suspend fun getAccountSummary(): IbkrAccountSummary {
        val client = session.getClient()
        return client.getAccountSummary()
    }

    //===========================================================//

    suspend fun getHistoricalData(
        securityIdentifier: SecurityIdentifier,
        from: Instant,
        to: Instant
    ): List<IbkrHistoricalBar> {
        val client = session.getClient()

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

    suspend fun reserveOrderId(): Int {
        val client = session.getClient()
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