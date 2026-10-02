package application.service.portfolio

import domain.Portfolio
import java.util.*

interface IPortfolioService {
    suspend fun update(userId: UUID, portfolio: Portfolio): Portfolio
    suspend fun create(userId: UUID): Portfolio
    suspend fun get(userId: UUID): Portfolio
    // suspend fun deletePortfolio() Not yet needed
    // suspend fun deleteAllPortfolio() Not yet needed
}