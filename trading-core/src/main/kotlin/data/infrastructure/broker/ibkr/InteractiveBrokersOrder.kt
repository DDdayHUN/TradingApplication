package data.infrastructure.broker.ibkr

import data.infrastructure.broker.ibkr.InteractiveBrokersOrder.Status
import domain.trader.TradingOrder

data class InteractiveBrokersOrder(
    val tradingOrder: TradingOrder,
    val brokerOrderId: Int,
    val status: Status = Status.PENDING,
    val filledQuantity: String = "",
    val averageFillPrice: Double? = null
) {
    fun submit(): Result<InteractiveBrokersOrder> {
        return when (status) {
            Status.PENDING -> {
                Result.success(copy(status = Status.SUBMITTED))
            }

            Status.SUBMITTED, Status.FILLED -> {
                Result.success(this)
            }

            else -> {
                Result.failure(
                    IllegalStateException("Cannot submit order with status $status")
                )
            }
        }
    }

    fun fill(
        filledQuantity: String,
        averageFillPrice: Double
    ): Result<InteractiveBrokersOrder> {
        return when (status) {
            Status.PENDING, Status.SUBMITTED -> {
                Result.success(
                    copy(
                        status = Status.FILLED,
                        filledQuantity = filledQuantity,
                        averageFillPrice = averageFillPrice
                    )
                )
            }

            Status.FILLED -> {
                Result.success(this)
            }

            else -> {
                Result.failure(
                    IllegalStateException("Cannot fill order with status $status")
                )
            }
        }
    }

    fun cancel(): Result<InteractiveBrokersOrder> {
        return when (status) {
            Status.PENDING, Status.SUBMITTED -> {
                Result.success(copy(status = Status.CANCELLED))
            }

            Status.CANCELLED -> {
                Result.success(this)
            }

            else -> {
                Result.failure(
                    IllegalStateException("Cannot cancel order with status $status")
                )
            }
        }
    }

    enum class Status {
        PENDING,
        SUBMITTED,
        PRE_SUBMITTED,
        FILLED,
        CANCELLED,
        INACTIVE,
        PENDING_CANCEL
    }
}

fun TradingOrder.toInteractiveBrokersOrder(ibkrOrderId: Int): InteractiveBrokersOrder {
    return InteractiveBrokersOrder(
        tradingOrder = this,
        brokerOrderId = ibkrOrderId,
        status = Status.PENDING,
    )
}

fun InteractiveBrokersOrder.toDomain(): TradingOrder {
    return tradingOrder
}