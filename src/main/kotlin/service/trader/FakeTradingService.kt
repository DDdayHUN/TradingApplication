package service.trader

import domain.trader.TradingOrder

@Deprecated("This is only a fake service")
class FakeTradingService : ITradingService {
    override suspend fun putOrder(order: TradingOrder, buyPrice: Double): TradingResult {
        return TradingResult.Success(order)
    }
}