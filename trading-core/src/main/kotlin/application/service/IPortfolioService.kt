package application.service

import domain.Portfolio
import java.util.UUID

interface IPortfolioService {
    suspend fun save(portfolio: Portfolio): Portfolio
    suspend fun createPortfolio(): Portfolio
    suspend fun getAllPortfolio(): List<Portfolio>
    suspend fun getPortfolio(id: UUID): Portfolio
    suspend fun deleteAllPortfolio(): Boolean
    suspend fun deletePortfolio(id: UUID): Boolean
}