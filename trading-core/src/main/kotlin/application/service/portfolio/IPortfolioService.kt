package application.service.portfolio

import domain.Portfolio
import java.util.*

interface IPortfolioService {
    suspend fun save(portfolio: Portfolio): Portfolio
    suspend fun create(): Portfolio
    suspend fun getAll(): List<Portfolio>
    suspend fun getById(portfolioId: UUID): Portfolio
    suspend fun delete(portfolioId: UUID): Boolean
}