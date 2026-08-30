package domain.interfaces

import domain.Portfolio
import domain.User
import java.util.UUID

interface IPortfolioRepository {
    suspend fun create(userId: UUID, portfolio: Portfolio): Result<Portfolio>
    suspend fun save(portfolio: Portfolio): Result<Portfolio>
    suspend fun getById(id: UUID): Result<Portfolio>
    suspend fun getByIdForUser(userId: UUID, id: UUID): Result<Portfolio>
    suspend fun getAllByUserId(userId: UUID): Result<List<Portfolio>>
}