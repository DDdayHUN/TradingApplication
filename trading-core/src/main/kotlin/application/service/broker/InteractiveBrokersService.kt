package application.service.borker

import application.logging.logger
import application.service.broker.BrokerOrderRequest
import application.service.broker.IBrokerService
import com.ib.client.Contract
import com.ib.client.Decimal
import com.ib.client.Order
import infrastructure.broker.IbkrClient
import org.springframework.stereotype.Service


@Service
class InteractiveBrokersService(
    private val ibkrClient: IbkrClient,
) : IBrokerService {

    //===========================================================//
    //===========================================================//
    // Private Field(s)

    private val logger = logger<InteractiveBrokersService>()

    //===========================================================//
    //===========================================================//
    // Public Method(s)

    override suspend fun connect() {
        ibkrClient.connect(
            host = "127.0.0.1",
            port = 4002,
            clientId = 1
        )
    }

    override fun disconnect() {
        ibkrClient.disconnect()
    }

    override fun isConnected(): Boolean {
        return ibkrClient.isConnected()
    }

    override fun placeOrder(
        request: BrokerOrderRequest
    ): Int {
        require(request.ticker.isNotBlank()) {
            "Ticker must not be blank"
        }

        require(request.currency.isNotBlank()) {
            "Currency must not be blank"
        }

        require(request.quantity > 0) {
            "Quantity must be greater than zero"
        }

        val contract = createStockContract(
            request
        )

        val order = createMarketOrder(
            request
        )

        logger.info(
            "Submitting broker order ticker={} side={} quantity={}",
            request.ticker,
            request.side,
            request.quantity
        )

        return ibkrClient.placeOrder(
            contract = contract,
            order = order
        )
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

    private fun createMarketOrder(
        request: BrokerOrderRequest
    ): Order {
        return Order().apply {
            action(request.side.name)

            orderType("MKT")

            totalQuantity(
                Decimal.get(request.quantity)
            )

            tif("DAY")
        }
    }
}