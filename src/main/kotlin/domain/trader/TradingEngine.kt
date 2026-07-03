package domain.trader

import domain.assets.Quote
import domain.signal.TradingSignal
import domain.signal.TradingDispatcher

class TradingEngine {
    private val m_signalDispatcher: TradingDispatcher

    suspend fun onQuote(trader: Trader, quote: Quote): TradingSignal{
        val signal = trader.createSignal(quote)
        println(signal.toReadableText())

        m_signalDispatcher.dispatch(trader, signal)

        return signal
    }

    constructor(signalDispatcher: TradingDispatcher){
        this.m_signalDispatcher = signalDispatcher
    }
}