package application.service.broker

import domain.order.Order

data class BrokerOrderRequest(
    val ticker: String,
    val currency: String,
    val quantity: Double,
    val side: Order.OrderAction
)