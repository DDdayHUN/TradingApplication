package application.service

import api.dto.CreateTraderRequest
import domain.market.security.SecurityHolding
import domain.order.OrderAction
import domain.trader.Trader
import domain.trader.TradingOrder
import java.util.UUID

interface ITraderService {
    suspend fun createTrader(userId: UUID, portfolioId: UUID, request: CreateTraderRequest): Trader
    suspend fun getAllByPortfolioId(userId: UUID, portfolioId: UUID): Set<Trader>
    suspend fun getById(userId: UUID, portfolioId: UUID, traderId: UUID): Trader?
    suspend fun executeTrader(portfolioId: UUID, traderId: UUID): TradingOrder
    suspend fun applyFill(portfolioId: UUID, traderId: UUID, action: OrderAction, filledQuantity: String, averagePrice: Double,sellBatches: List<Pair<SecurityHolding, Int>>)
}