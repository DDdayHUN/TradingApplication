package application.service.broker

import application.logging.logger
import com.ib.client.Contract
import com.ib.client.Decimal
import com.ib.client.Order
import domain.market.security.SecurityIdentifier
import infrastructure.broker.IbkrAccountSummary
import infrastructure.broker.IbkrHistoricalBar
import infrastructure.broker.IbkrSession
import kotlinx.coroutines.delay
import org.springframework.stereotype.Service
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Instant


@Service
class InteractiveBrokersService(
    private val session: IbkrSession,
) : IBrokerService {

    //===========================================================//
    //===========================================================//
    // Private Field(s)

    private val logger = logger<InteractiveBrokersService>()

    //===========================================================//
    //===========================================================//
    // Public Method(s)

    override suspend fun placeOrder(orderId: Int, request: BrokerOrderRequest): Int {
        require(request.ticker.isNotBlank()) { "Ticker must not be blank" }
        require(request.currency.isNotBlank()) { "Currency must not be blank" }
        require(request.quantity > 0) { "Quantity must be greater than zero" }

        val client = session.getClient()
        val contract = createStockContract(request)
        val order = createMarketOrder(request)

        logger.info(
            "Submitting broker order ticker={} side={} quantity={}",
            request.ticker,
            request.side,
            request.quantity
        )

        return client.placeOrder(
            orderId = orderId,
            contract = contract,
            order = order
        )
    }

    //===========================================================//

    override suspend fun requestOrderStatus() {
        val client = session.getClient()
        client.requestOpenOrders()
    }

    //===========================================================//

    override suspend fun getAccountSummary(): IbkrAccountSummary {
        val client = session.getClient()
        return client.getAccountSummary()
    }

    //===========================================================//

    override suspend fun getNextOrderId(): Int {
        val client = session.getClient()
        return client.getNextOrderId()
    }

    //===========================================================//

    override suspend fun getHistoricalData(
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
    //===========================================================//
    // Private Method(s)

    private fun createStockContract(
        request: BrokerOrderRequest
    ): Contract {
        return Contract().apply {
            symbol(request.ticker)
            secType("STK")
            exchange("SMART")
            currency(request.currency)
        }
    }

    //===========================================================//

    private fun createMarketOrder(request: BrokerOrderRequest): Order {
        return Order().apply {
            action(request.side.name)
            orderType("MKT")
            totalQuantity(Decimal.get(request.quantity))
            tif("DAY")
        }
    }
}