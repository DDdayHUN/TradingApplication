package data.infrastructure.broker.ibkr

import domain.market.security.SecurityIdentifier
import domain.trader.TradingOrder
import java.time.Instant
import java.util.UUID

data class InteractiveBrokersOrder(
    val id: UUID,
    val traderId: UUID,
    val brokerOrderId: Int,
    val securityIdentifier: SecurityIdentifier,
    val signal: TradingOrder.Signal,
    val action: Action,
    val signalPrice: Double,
    val status: Status = Status.PENDING,
    val filledQuantity: String = "",
    val averageFillPrice: Double? = null,
    val createdAt: Instant
) {
    val quantity: Double
        get() = when (signal) {
            is TradingOrder.Signal.Buy -> signal.amount.toDouble()
            is TradingOrder.Signal.Sell -> signal.sellHoldings.sumOf { (_, amount) -> amount }.toDouble()
        }

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

    enum class Action {
        BUY,
        SELL
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
        id = id,
        traderId = traderId,
        brokerOrderId = ibkrOrderId,
        securityIdentifier = securityIdentifier,
        signal = signal,
        action = InteractiveBrokersOrder.Action.BUY,
        signalPrice = atPrice,
        status = InteractiveBrokersOrder.Status.PENDING,
        createdAt = createdAt
    )
}

fun InteractiveBrokersOrder.toDomain(): TradingOrder {
    return TradingOrder(
        id = id,
        traderId = traderId,
        securityIdentifier = securityIdentifier,
        signal = signal,
        atPrice = signalPrice,
        createdAt = createdAt
    )
}