package infrastructure.broker

sealed interface IbkrEvent {
    val orderId: Int
    data class OrderSubmittedEvent( override val orderId: Int): IbkrEvent
    data class OrderFilledEvent( override val orderId: Int, val filled: String, val averageFillPrice: Double): IbkrEvent
    data class OrderCancelledEvent( override val orderId: Int): IbkrEvent
}