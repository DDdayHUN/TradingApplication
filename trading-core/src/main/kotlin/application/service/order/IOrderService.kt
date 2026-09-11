package application.service.order

import domain.order.Order
import infrastructure.broker.IbkrEvent

interface IOrderService {
    suspend fun submit(order: Order)
    suspend fun handle(event: IbkrEvent.OrderSubmittedEvent)
    suspend fun handle(event: IbkrEvent.OrderCancelledEvent)
    suspend fun handle(event: IbkrEvent.OrderFilledEvent)
}