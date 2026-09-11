package application.service.portfolio

import domain.Portfolio
import infrastructure.broker.IbkrAccountSummary
import java.util.UUID

interface IPortfolioService {
    suspend fun save(portfolio: Portfolio): Portfolio
    suspend fun createPortfolio(): Portfolio
    suspend fun getAllPortfolio(): List<Portfolio>
    suspend fun getPortfolio(portfolioId: UUID): Portfolio
    suspend fun getPortfolioByTraderId(traderId: UUID): Portfolio
    suspend fun deleteAllPortfolio(): Boolean
    suspend fun deletePortfolio(portfolioId: UUID): Boolean
    suspend fun getAccountSummary(portfolioId: UUID): IbkrAccountSummary
}