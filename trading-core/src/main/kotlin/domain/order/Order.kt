package domain.order

import domain.algorithm.TradingAlgorithm
import domain.market.security.SecurityIdentifier
import infrastructure.broker.SellAllocation
import java.time.Instant
import java.util.*

data class Order(
    val id: UUID = UUID.randomUUID(),
    val ibkrOrderId: Int? = null,

    val traderId: UUID,
    val securityIdentifier: SecurityIdentifier,

    val signal: Signal,
    val signalPrice: Double,

    val status: Status = Status.PENDING,
    val filledQuantity: String = "",
    val averageFillPrice: Double? = null,

    val createdAt: Instant = Instant.now()
){

    sealed interface Signal {
        data class Buy(val amount: Int): Signal
        data class Sell(val allocations: List<SellAllocation>): Signal
    }

    fun withIbkrOrderId(ibkrOrderId: Int): Order {
        return copy(
            ibkrOrderId = ibkrOrderId
        )
    }

    val quantity: Double
        get() = when (val signal = signal) {
            is Signal.Buy ->
                signal.amount.toDouble()

            is Signal.Sell ->
                signal.allocations.sumOf { (_, amount) -> amount }.toDouble()
        }


    fun submit(): Result<Order> {
        try {
            check(status == Status.PENDING) { "Status must be PENDING" }
            return Result.success(copy(status = Status.SUBMITTED))
        }
        catch(e: Exception) {
            return Result.failure(e)
        }
    }

    fun fill(filledQuantity: String, averageFillPrice: Double): Result<Order> {
        try {
            check(status != Status.FILLED && status != Status.CANCELLED) { "Status must be PENDING" }
            return Result.success(copy(status = Status.FILLED, filledQuantity = filledQuantity, averageFillPrice = averageFillPrice))
        }
        catch(e: Exception) {
            return Result.failure(e)
        }
    }

    fun cancel(): Result<Order> {
        try {
            check(status != Status.FILLED) { "Status is already FILLED" }
            return Result.success(copy(status = Status.CANCELLED))
        }
        catch(e: Exception) {
            return Result.failure(e)
        }
    }

    companion object {
        fun fromAlgorithm(traderId: UUID, securityIdentifier: SecurityIdentifier, output: TradingAlgorithm.Output, atPrice: Double): Order?{
            val buy = output.buy
            val sell = output.sell

            return when {

                buy != null && sell == null -> {
                    Order(
                        traderId = traderId,
                        securityIdentifier = securityIdentifier,
                        signal = Signal.Buy(
                            buy.amount
                        ),
                        signalPrice = atPrice,
                    )
                }

                buy == null && sell != null-> {
                    Order(
                        traderId = traderId,
                        securityIdentifier = securityIdentifier,
                        signal = Signal.Sell(
                            allocations = sell.batches.map { (holding, amount) ->
                                SellAllocation(
                                    holdingId = holding.id,
                                    amount = amount
                                )
                            }
                        ),
                        signalPrice = atPrice,
                    )
                }
                buy == null && sell == null -> null
                else -> {
                    throw IllegalStateException("Buy and sell are not supported at the same time")
                }
            }
        }
    }

    enum class OrderAction {
        BUY,
        SELL
    }

    enum class Status {
        PENDING,
        SUBMITTED,
        PARTIALLY_FILLED,
        FILLED,
        CANCELLED,
        REJECTED
    }

}
