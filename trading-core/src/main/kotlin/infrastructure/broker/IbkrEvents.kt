package infrastructure.broker

sealed interface IbkrOrderEvent {
    val orderId: Int
}

data class OrderSubmittedEvent(
    override val orderId: Int
) : IbkrOrderEvent

data class OrderFilledEvent(
    override val orderId: Int,
    val filled: String,
    val averageFillPrice: Double
): IbkrOrderEvent

data class OrderCancelledEvent(
    override val orderId: Int
): IbkrOrderEvent