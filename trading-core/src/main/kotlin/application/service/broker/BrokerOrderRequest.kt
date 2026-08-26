package application.service.broker

import domain.trader.TradingOrder

enum class BrokerOrderSide{
    BUY,
    SELL
}

data class BrokerOrderRequest(
    val ticker: String,
    val currency: String,
    val quantity: Double,
    val side: BrokerOrderSide
)

fun TradingOrder.toBrokerOrder(): BrokerOrderRequest? {
    if(this.buy == null && this.sell == null) return null
    val quantity: Double
    val side: BrokerOrderSide

    if(this.buy != null){
        quantity = this.buy.amount.toDouble()
        side = BrokerOrderSide.BUY
    }else {
        quantity = this.sell!!.batches.sumOf {(_,amount) -> amount}.toDouble()
        side = BrokerOrderSide.SELL
    }
    return BrokerOrderRequest(
        ticker = this.securityIdentifier.tickerSymbol,
        currency = this.securityIdentifier.currency,
        quantity = quantity,
        side = side
    )
}