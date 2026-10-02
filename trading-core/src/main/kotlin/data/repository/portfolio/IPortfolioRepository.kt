package data.repository.portfolio

import data.repository.portfolio.sql.PortfolioEntity
import domain.Portfolio
import java.util.UUID

interface IPortfolioRepository {
    // TODO : 'Remove', because imo in this layer we should not use users this belongs in the service layer, to correctly call repositories.
    // TODO : We solve the connection problem to the User not in PorttfolioEntity but in UserEntity and User class as a whole.
    // suspend fun create(userId: UUID, portfolio: Portfolio): Result<Portfolio> // TODO : Can be freely removed
    suspend fun save(portfolio: PortfolioEntity): Result<PortfolioEntity>
    suspend fun getById(portfolioId: UUID): Result<PortfolioEntity>
    // suspend fun getByIdForUser(userId: UUID, id: UUID): Result<Portfolio>  // TODO : Can be freely removed
    // suspend fun getByUserId(userId: UUID): Result<Portfolio>  // TODO : Can be freely removed
    // TODO : suspend fun getAll(): Result<List<PortfolioEntity>>
    // TODO : suspend fun deleteById(portfolioId: UUID): Result<Unit>
    // TODO : suspend fun deleteAll(): Result<Unit>
}