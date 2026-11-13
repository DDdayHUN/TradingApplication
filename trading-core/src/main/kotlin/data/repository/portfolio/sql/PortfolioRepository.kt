package data.repository.portfolio.sql

import data.repository.portfolio.IPortfolioRepository
import exception.PortfolioNotFoundException
import org.springframework.stereotype.Repository
import java.util.*

@Repository
class PortfolioRepository(
    private val repository: IPortfolioJpaRepository
) : IPortfolioRepository {
    //===========================================================//
    //===========================================================//
    // Public Method(s)

    override suspend fun save(portfolio: PortfolioEntity): Result<PortfolioEntity> {
       return runCatching {
           repository.save(portfolio)
       }
    }

    //===========================================================//

    override suspend fun getById(portfolioId: UUID): Result<PortfolioEntity> {
        return runCatching {
            repository.findWithRelationsById(portfolioId)
                ?: throw PortfolioNotFoundException(portfolioId)
        }
    }
}