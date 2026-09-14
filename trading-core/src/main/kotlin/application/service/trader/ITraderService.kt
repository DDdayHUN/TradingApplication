package application.service.trader

import api.dto.ChangeTraderAlgorithmRequest
import api.dto.CreateTraderRequest
import domain.trader.SellHolding
import domain.trader.Trader
import domain.trader.TradingOrder
import java.util.UUID

interface ITraderService {
    suspend fun createTrader(portfolioId: UUID, request: CreateTraderRequest): Trader
    suspend fun getAllByPortfolioId(portfolioId: UUID): Set<Trader>
    suspend fun getById(traderId: UUID): Trader
    suspend fun changeAlgorithm(traderId: UUID, request: ChangeTraderAlgorithmRequest): Trader
    suspend fun executeTrader(traderId: UUID): TradingOrder?
    suspend fun applyBuyFill(traderId: UUID, filledQuantity: Int, averageFillPrice: Double)
    suspend fun applySellFill(traderId: UUID, sellAllocations: Set<SellHolding>, averageFillPrice: Double)
    suspend fun forceSellHolding(traderId: UUID, securityHoldingId: UUID): TradingOrder
    suspend fun forceSellAllHolding(traderId: UUID): List<TradingOrder>
}