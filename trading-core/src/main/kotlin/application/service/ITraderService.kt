package application.service

import api.dto.CreateTraderRequest
import domain.trader.Trader
import java.util.UUID

interface ITraderService {
    suspend fun createTrader(portfolioId: UUID, request: CreateTraderRequest): Trader
    suspend fun getAllByPortfolioId(portfolioId: UUID): Set<Trader>
    suspend fun getById(portfolioId: UUID, traderId: UUID): Trader?
}