package data.repository.portfolio

import data.repository.portfolio.sql.PortfolioEntity
import java.util.UUID

interface IPortfolioRepository {
    suspend fun save(portfolio: PortfolioEntity): Result<PortfolioEntity>

    suspend fun getById(portfolioId: UUID): Result<PortfolioEntity>
}