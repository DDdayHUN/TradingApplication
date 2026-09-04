package data.repository.portfolio

import domain.Portfolio
import java.util.UUID

interface IPortfolioRepository {
    suspend fun create(userId: UUID, portfolio: Portfolio): Result<Portfolio>
    suspend fun save(portfolio: Portfolio): Result<Portfolio>
    suspend fun getById(id: UUID): Result<Portfolio>
    suspend fun getByIdForUser(userId: UUID, id: UUID): Result<Portfolio>
    suspend fun getAllByUserId(userId: UUID): Result<List<Portfolio>>
    suspend fun getByTraderId(traderId: UUID): Result<Portfolio>
}