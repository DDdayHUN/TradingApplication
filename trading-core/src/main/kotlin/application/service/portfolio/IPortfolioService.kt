package application.service.portfolio

import domain.Portfolio
import domain.Portfolio.PortfolioAccountSummary
import java.util.*

interface IPortfolioService {
    suspend fun save(portfolio: Portfolio): Portfolio
    suspend fun createPortfolio(): Portfolio
    suspend fun getPortfolio(): Portfolio
    suspend fun deletePortfolio(): Boolean
    suspend fun getPortfolioAccountSummary():PortfolioAccountSummary
}