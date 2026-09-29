package application.service.portfolio

import domain.Portfolio
import domain.Portfolio.PortfolioAccountSummary
import java.util.*

interface IPortfolioService {
    suspend fun save(portfolio: Portfolio): Portfolio
    suspend fun createPortfolio(): Portfolio
    suspend fun getAllPortfolio(): List<Portfolio>
    suspend fun getPortfolio(portfolioId: UUID): Portfolio
    suspend fun deletePortfolio(portfolioId: UUID): Boolean
    suspend fun getPortfolioAccountSummary(portfolioId: UUID):PortfolioAccountSummary
}