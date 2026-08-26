package application.service.borker

import application.logging.logger
import application.service.broker.BrokerOrderRequest
import application.service.broker.IBrokerService
import com.ib.client.Contract
import com.ib.client.Decimal
import com.ib.client.Order
import infrastructure.broker.IbkrClient
import infrastructure.broker.IbkrSession
import org.springframework.stereotype.Service


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

    override suspend fun placeOrder(request: BrokerOrderRequest): Int {
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
            contract = contract,
            order = order
        )
    }

    override suspend fun requestOrderStatus() {
        val client = session.getClient()
        client.requestOpenOrders()
    }

    override suspend fun getAvailableCapital(): Double {
        val client = session.getClient()
        return client.getAvailableCapital()
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

    private fun createMarketOrder(request: BrokerOrderRequest): Order {
        return Order().apply {
            action(request.side.name)
            orderType("MKT")
            totalQuantity(Decimal.get(request.quantity))
            tif("DAY")
        }
    }
}