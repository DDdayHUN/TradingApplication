package application.service.broker

import domain.order.OrderAction

data class BrokerOrderRequest(
    val ticker: String,
    val currency: String,
    val quantity: Double,
    val side: OrderAction
)