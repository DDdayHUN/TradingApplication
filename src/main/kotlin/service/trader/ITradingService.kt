package service.trader

import domain.algorithm.TradingAlgorithm
import domain.assets.security.SecurityIdentifier

//===========================================================//
/**
 * Executes buy and sell orders
 */
//===========================================================//
interface ITradingService {
    suspend fun executeBuyAsync(
        securityIdentifier: SecurityIdentifier,
        buy: TradingAlgorithm.Output.Buy,
        buyPrice: Double,
    ): TradingResult

    suspend fun executeSellAsync(
        securityIdentifier: SecurityIdentifier,
        sell: TradingAlgorithm.Output.Sell,
        sellPrice: Double,
    ): TradingResult
}