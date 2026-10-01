package application.service.portfolio

import domain.Portfolio
import domain.Portfolio.PortfolioAccountSummary
import java.util.*

interface IPortfolioService {
    suspend fun save(portfolio: Portfolio): Portfolio
    suspend fun createPortfolio(userId: UUID): Portfolio
    suspend fun getPortfolioByUserId(userId: UUID): Portfolio
    suspend fun deletePortfolio(userId: UUID): Boolean
    suspend fun getPortfolioAccountSummary(userId: UUID): PortfolioAccountSummary
}