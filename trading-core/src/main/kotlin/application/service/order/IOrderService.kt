package application.service.order

import domain.order.Order
import infrastructure.broker.OrderCancelledEvent
import infrastructure.broker.OrderFilledEvent
import infrastructure.broker.OrderSubmittedEvent

interface IOrderService {
    suspend fun submit(order: Order)
    suspend fun handleOrderSubmitted(event: OrderSubmittedEvent)
    suspend fun handleOrderCancelled(event: OrderCancelledEvent)
    suspend fun handleOrderFilled(event: OrderFilledEvent)
}