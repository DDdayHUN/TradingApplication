package application.service.broker

enum class BrokerOrderSide{
    BUY,
    SELL
}

data class BrokerOrderRequest(
    val ticker: String,
    val currency: String,
    val quantity: Double,
    val side: BrokerOrderSide
)
