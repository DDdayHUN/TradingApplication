package application.service

import domain.trader.TradingOrder
import infrastructure.broker.OrderCancelledEvent
import infrastructure.broker.OrderFilledEvent
import infrastructure.broker.OrderSubmittedEvent

interface IOrderService {
    suspend fun submit(order: TradingOrder)
    suspend fun handleOrderSubmitted(event: OrderSubmittedEvent)
    suspend fun handleOrderCancelled(event: OrderCancelledEvent)
    suspend fun handleBuyOrderFilled(event: OrderFilledEvent)
}