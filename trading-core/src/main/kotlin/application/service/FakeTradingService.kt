package application.service

import domain.interfaces.ITradingService
import domain.trader.TradingOrder

@Deprecated("This is only a fake data.service")
internal object FakeTradingService : ITradingService {
    override suspend fun putOrder(order: TradingOrder): Result<TradingOrder> {
        return Result.success(order)
    }
}