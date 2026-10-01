package application.service.portfolio

import domain.Portfolio
import java.util.*

interface IPortfolioService {
    suspend fun save(portfolio: Portfolio): Portfolio
    suspend fun createPortfolio(userId: UUID): Portfolio
    suspend fun getPortfolioByUserId(userId: UUID): Portfolio
    suspend fun deletePortfolio(userId: UUID): Boolean
}