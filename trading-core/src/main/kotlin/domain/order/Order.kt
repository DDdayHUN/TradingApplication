package domain.order

import domain.trader.TradingOrder
import java.time.Instant
import java.util.*

data class Order(
    val id: UUID,
    val ibkrOrderId: Int,
    val traderId: UUID,
    val action: OrderAction,
    val quantity: Double,
    val signalPrice: Double,
    val status: OrderStatus,
    val filledQuantity: String = "",
    val averageFillPrice: Double? = null,
    val createdAt: Instant
){
    fun submitted(): Order {
        if(status != OrderStatus.PENDING) return this

        return copy(
            status = OrderStatus.SUBMITTED
        )
    }

    fun filled(filledQuantity: String, averageFillPrice: Double): Order {
        if(status == OrderStatus.FILLED) return this
        return copy(
            status = OrderStatus.FILLED,
            filledQuantity = filledQuantity,
            averageFillPrice = averageFillPrice
        )
    }

    fun cancelled(): Order {
        if(status == OrderStatus.FILLED) return this

        return copy(
            status = OrderStatus.CANCELLED
        )
    }
}


enum class OrderAction {
    BUY,
    SELL
}

enum class OrderStatus {
    PENDING,
    SUBMITTED,
    PARTIALLY_FILLED,
    FILLED,
    CANCELLED,
    REJECTED
}

fun TradingOrder.toOrder(ibkrOrderId: Int): Order {
    val action: OrderAction
    val quantity: Double

    when {
        buy != null && sell == null -> {
            action = OrderAction.BUY
            quantity = buy.amount.toDouble()
        }

        sell != null && buy == null -> {
            action = OrderAction.SELL
            quantity = sell.batches
                .sumOf { (_, amount) -> amount }
                .toDouble()
        }

        else -> throw IllegalArgumentException(
            "TradingOrder must contain exactly one action"
        )
    }

    return Order(
        id = orderId,
        ibkrOrderId = ibkrOrderId,
        traderId = traderId,
        action = action,
        quantity = quantity,
        signalPrice = atPrice,
        status = OrderStatus.PENDING,
        createdAt = createdAt
    )
}
