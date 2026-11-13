package data.infrastructure.broker.ibkr

import java.util.UUID

sealed interface IbkrEvent {
    val userId: UUID
    val orderId: Int

    data class OrderSubmittedEvent(override val userId: UUID, override val orderId: Int): IbkrEvent
    data class OrderFilledEvent(override val userId: UUID, override val orderId: Int, val filled: String, val averageFillPrice: Double): IbkrEvent
    data class OrderCancelledEvent(override val userId: UUID, override val orderId: Int): IbkrEvent
    data class OrderPendingEvent(override val userId: UUID, override val orderId: Int): IbkrEvent
    data class OrderPreSubmittedEvent(override val userId: UUID, override val orderId: Int): IbkrEvent
    data class OrderPendingCancelEvent(override val userId: UUID, override val orderId: Int): IbkrEvent
    data class OrderInactiveEvent(override val userId: UUID, override val orderId: Int): IbkrEvent
}