package service.trader

import domain.trader.TradingOrder

sealed interface ITradingService {
    suspend fun putOrder(order: TradingOrder, buyPrice: Double): TradingResult
}