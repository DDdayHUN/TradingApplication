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