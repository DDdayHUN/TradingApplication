package application.handlers

import domain.signal.TradingSignal
import domain.trader.Trader


interface ITradingHandler {
    suspend fun handle(trader: Trader, signal: TradingSignal)
}