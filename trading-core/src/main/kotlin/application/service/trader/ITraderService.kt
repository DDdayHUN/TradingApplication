package application.service.trader

import api.dto.ChangeTraderAlgorithmRequest
import api.dto.CreateTraderRequest
import application.model.User
import domain.trader.SellHolding
import domain.trader.Trader
import domain.trader.TradingOrder
import java.util.*

interface ITraderService {
    suspend fun createTrader(userId: UUID, request: CreateTraderRequest): Trader
    suspend fun deleteTrader(userId: UUID, traderId: UUID)
    suspend fun getAll(userId: UUID): Set<Trader>
    suspend fun getById(userId: UUID, traderId: UUID): Trader
    suspend fun changeAlgorithm(userId: UUID, traderId: UUID, request: ChangeTraderAlgorithmRequest): Trader
    suspend fun executeTrader(userId: UUID, traderId: UUID): TradingOrder?
    suspend fun applyBuyFill(traderId: UUID, filledQuantity: Int, averageFillPrice: Double)
    suspend fun applySellFill(traderId: UUID, sellAllocations: Set<SellHolding>, averageFillPrice: Double)
    suspend fun forceSellHolding(userId: UUID, traderId: UUID, securityHoldingId: UUID): TradingOrder
    suspend fun forceSellAllHolding(userId: UUID, traderId: UUID): List<TradingOrder>
}