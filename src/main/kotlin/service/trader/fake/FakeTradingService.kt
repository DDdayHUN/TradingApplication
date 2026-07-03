package service.trader.fake

import domain.algorithm.TradingAlgorithm
import domain.assets.security.SecurityIdentifier
import service.trader.ITradingService
import service.trader.TradingResult

@Deprecated("This is only a fake service")
class FakeTradingService : ITradingService {
     override suspend fun executeBuyAsync(
        securityIdentifier: SecurityIdentifier,
        buy: TradingAlgorithm.Output.Buy,
        buyPrice: Double
    ): TradingResult {
        return TradingResult.success()
    }

     override suspend fun executeSellAsync(
        securityIdentifier: SecurityIdentifier,
        sell: TradingAlgorithm.Output.Sell,
        sellPrice: Double
    ): TradingResult {
        return TradingResult.success()
    }

}