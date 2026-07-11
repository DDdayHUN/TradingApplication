package domain.interfaces

import domain.trader.TradingOrder

interface ITradingService {
    suspend fun putOrder(order: TradingOrder, buyPrice: Double): Result<TradingOrder>
}