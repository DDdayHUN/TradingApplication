package application.service.portfolio

import domain.Portfolio
import infrastructure.broker.IbkrAccountSummary
import java.util.UUID

interface IPortfolioService {
    suspend fun save(portfolio: Portfolio): Portfolio
    suspend fun createPortfolio(userId: UUID): Portfolio
    suspend fun getAllPortfolio(userId: UUID): List<Portfolio>
    suspend fun getPortfolio(userId: UUID, id: UUID): Portfolio
    suspend fun getPortfolio(portfolioId: UUID): Portfolio
    suspend fun getPortfolioByTraderId(traderId: UUID): Portfolio
    suspend fun deleteAllPortfolio(userId: UUID): Boolean
    suspend fun deletePortfolio(userId: UUID, id: UUID): Boolean
    suspend fun getAccountSummary(portfolioId: UUID): IbkrAccountSummary
}