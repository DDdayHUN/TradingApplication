package application.service

import api.dto.CreateTraderRequest
import domain.trader.Trader
import domain.trader.TradingOrder
import infrastructure.broker.SellAllocation
import java.util.*

interface ITraderService {
    suspend fun createTrader(userId: UUID, portfolioId: UUID, request: CreateTraderRequest): Trader
    suspend fun getAllByPortfolioId(userId: UUID, portfolioId: UUID): Set<Trader>
    suspend fun getById(userId: UUID, portfolioId: UUID, traderId: UUID): Trader?
    suspend fun executeTrader(portfolioId: UUID, traderId: UUID): TradingOrder
    suspend fun applyBuyFill(traderId: UUID, filledQuantity: Int, averageFillPrice: Double)
    suspend fun applySellFill(traderId: UUID, sellAllocations: List<SellAllocation>, averageFillPrice: Double)
}