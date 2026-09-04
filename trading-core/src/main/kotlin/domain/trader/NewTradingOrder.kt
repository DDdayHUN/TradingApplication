package domain.trader

import domain.market.security.SecurityHolding
import java.time.Instant
import java.util.UUID

data class NewTradingOrder(
    val orderId: UUID,
    val traderId: UUID,
    val securityId: UUID,
    val price: Double,
    val signal: Signal,
    val status: Status,
    val createdAt: Instant
) {
    sealed interface Signal {
        data class Buy(val amount: Int) : Signal
        data class Sell(val batches: Set<Pair<SecurityHolding, Int>>) : Signal
    }

    enum class Status {
        PENDING,
        SUBMITTED,
        FILLED,
        CANCELLED
    }

    fun submit(): Result<NewTradingOrder> {
        try {
            check(status == Status.PENDING) { "Status must be PENDING" }
            return Result.success(copy(status = Status.SUBMITTED))
        }
        catch(e: Exception) {
            return Result.failure(e)
        }
    }

    fun fill(): Result<NewTradingOrder> {
        try {
            check(status != Status.FILLED && status != Status.CANCELLED) { "Status must be PENDING" }
            return Result.success(copy(status = Status.FILLED))
        }
        catch(e: Exception) {
            return Result.failure(e)
        }
    }

    fun cancel(): Result<NewTradingOrder> {
        try {
            check(status != Status.FILLED) { "Status is already FILLED" }
            return Result.success(copy(status = Status.CANCELLED))
        }
        catch(e: Exception) {
            return Result.failure(e)
        }
    }
}