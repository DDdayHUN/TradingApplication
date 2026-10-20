package application.service.trader

import api.dto.CreateTraderRequest
import domain.algorithm.TradingAlgorithm
import domain.trader.SellHolding
import domain.trader.Trader
import domain.trader.TradingOrder
import java.util.*

interface ITraderService {
    suspend fun createTrader(userId: UUID, request: CreateTraderRequest): Trader
    suspend fun deleteTrader(userId: UUID, traderId: UUID)

    suspend fun changeAlgorithm(traderId: UUID, algorithmType: TradingAlgorithm.Type): Trader
    suspend fun executeAllTrader(userId: UUID): List<TradingOrder>
    suspend fun applyBuyFill(traderId: UUID, filledQuantity: Int, averageFillPrice: Double)
    suspend fun applySellFill(traderId: UUID, sellAllocations: Set<SellHolding>, averageFillPrice: Double)
    suspend fun forceSellAllHolding(userId: UUID, traderId: UUID): TradingOrder
    suspend fun forceSellAllHolding(userId: UUID): List<TradingOrder>
}