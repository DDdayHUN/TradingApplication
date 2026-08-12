package domain.interfaces

import domain.trader.TradingOrder

interface ITradingService {
    suspend fun putOrder(order: TradingOrder): Result<TradingOrder>
}