package domain.interfaces

import domain.Portfolio
import domain.User
import java.util.UUID

interface IPortfolioRepository {
    suspend fun save(user: User, portfolio: Portfolio): Result<Portfolio>
    suspend fun getById(id: UUID): Result<Portfolio>
    suspend fun getAllByUser(user: User): Result<List<Portfolio>>
}