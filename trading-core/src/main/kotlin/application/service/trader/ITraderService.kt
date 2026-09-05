package application.service.trader

import api.dto.ChangeTraderAlgorithmRequest
import api.dto.CreateTraderRequest
import domain.trader.Trader
import domain.trader.TradingOrder
import infrastructure.broker.SellAllocation
import java.util.UUID

interface ITraderService {
    suspend fun createTrader(userId: UUID, portfolioId: UUID, request: CreateTraderRequest): Trader
    suspend fun getAllByPortfolioId(portfolioId: UUID): Set<Trader>
    suspend fun getById(portfolioId: UUID, traderId: UUID): Trader?
    suspend fun changeAlgorithm(portfolioId: UUID, traderId: UUID, request: ChangeTraderAlgorithmRequest): Trader
    suspend fun executeTrader(portfolioId: UUID, traderId: UUID): TradingOrder
    suspend fun applyBuyFill(traderId: UUID, filledQuantity: Int, averageFillPrice: Double)
    suspend fun applySellFill(traderId: UUID, sellAllocations: List<SellAllocation>, averageFillPrice: Double)
    suspend fun forceSellHolding(traderId: UUID, securityHoldingId: UUID): TradingOrder
}