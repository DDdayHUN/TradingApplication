package domain.interfaces

import domain.Portfolio
import java.util.UUID

interface IPortfolioRepository {
    suspend fun save(portfolio: Portfolio): Result<Unit>
    suspend fun getById(id: UUID): Result<Portfolio>
    suspend fun getAllByUserId(userId: UUID): Result<List<Portfolio>>
    suspend fun getAll(): Result<List<Portfolio>>
}