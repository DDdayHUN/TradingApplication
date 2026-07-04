package service.trader

import domain.trader.TradingOrder

sealed interface TradingResult {
    data class Success(val order: TradingOrder) : TradingResult
    data object Failure : TradingResult
}