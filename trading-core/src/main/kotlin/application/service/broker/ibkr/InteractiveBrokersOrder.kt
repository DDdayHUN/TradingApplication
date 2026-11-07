package application.service.broker.ibkr

import domain.algorithm.TradingAlgorithm
import domain.market.security.SecurityHolding
import domain.market.security.SecurityIdentifier
import domain.trader.SellHolding
import domain.trader.TradingOrder
import java.time.Instant
import java.util.UUID

data class InteractiveBrokersOrder(
    val id: UUID = UUID.randomUUID(),
    val brokerOrderId: Int,
    val traderId: UUID,
    val securityIdentifier: SecurityIdentifier,
    val signal: Signal,
    val action: Action,
    val signalPrice: Double,
    val status: Status = Status.PENDING,
    val filledQuantity: String = "",
    val averageFillPrice: Double? = null,
    val createdAt: Instant = Instant.now()
) {
    val quantity: Double
        get() = when (signal) {
            is Signal.Buy -> signal.amount.toDouble()
            is Signal.Sell -> signal.allocations.sumOf { (_, amount) -> amount }.toDouble()
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

    sealed interface Signal {
        data class Buy(val amount: Int): Signal
        data class Sell(val allocations: Set<SellHolding>): Signal
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

fun TradingOrder.toInteractiveBrokersOrder(ibkrOrderId: Int): Set<InteractiveBrokersOrder> {
    val ret = mutableSetOf<InteractiveBrokersOrder>()

    if(this.signal.buy != null) {
        ret.add(InteractiveBrokersOrder(
            id = this.orderId,
            brokerOrderId = ibkrOrderId,
            traderId = this.traderId,
            signal = InteractiveBrokersOrder.Signal.Buy(signal.buy.amount),
            action = InteractiveBrokersOrder.Action.BUY,
            signalPrice = this.atPrice,
            status = InteractiveBrokersOrder.Status.PENDING,
            securityIdentifier = this.securityIdentifier,
            createdAt = this.createdAt
        ))
    }

    if(this.signal.sell != null) {
        val sell = signal.sell.batches.map { (holding, amount) ->
            SellHolding(
                id = holding.id,
                amount = amount
            )
        }.toSet()
        ret.add(InteractiveBrokersOrder(
            id = this.orderId,
            brokerOrderId = ibkrOrderId,
            traderId = this.traderId,
            signal = InteractiveBrokersOrder.Signal.Sell(sell),
            action = InteractiveBrokersOrder.Action.SELL,
            signalPrice = this.atPrice,
            status = InteractiveBrokersOrder.Status.PENDING,
            securityIdentifier = this.securityIdentifier,
            createdAt = this.createdAt
        ))
    }

    return ret
}

fun InteractiveBrokersOrder.toDomain(): TradingOrder {
    val algorithmOutput = when (val sig = this.signal) {
        is InteractiveBrokersOrder.Signal.Buy -> {
            TradingAlgorithm.Output(
                buy = TradingAlgorithm.Output.Buy(amount = sig.amount),
                sell = null
            )
        }
        is InteractiveBrokersOrder.Signal.Sell -> {
            val batches = sig.allocations.map { sellHolding ->
                Pair(
                    SecurityHolding(
                        id = sellHolding.id,
                        timestamp = createdAt,
                        purchasePrice = signalPrice,
                        amount = sellHolding.amount,
                    ),
                    sellHolding.amount
                )
            }.toSet()

            TradingAlgorithm.Output(
                buy = null,
                sell = TradingAlgorithm.Output.Sell(batches = batches)
            )
        }
    }

    return TradingOrder(
        orderId = id,
        traderId = traderId,
        securityIdentifier = securityIdentifier,
        signal = algorithmOutput,
        atPrice = signalPrice,
        createdAt = createdAt
    )
}