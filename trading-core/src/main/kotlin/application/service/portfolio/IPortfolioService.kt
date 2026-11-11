package application.service.portfolio

import data.repository.portfolio.sql.PortfolioEntity
import domain.Portfolio
import java.util.*

interface IPortfolioService {
    suspend fun updatePortfolio(portfolio: Portfolio): PortfolioEntity
}