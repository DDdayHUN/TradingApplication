package data.repository.portfolio.sql

import data.repository.portfolio.IPortfolioRepository
import exception.PortfolioNotFoundException
import org.springframework.stereotype.Repository
import java.util.*

@Repository
class PortfolioRepository(
    private val portfolioRepository: IPortfolioJpaRepository
) : IPortfolioRepository {
    //===========================================================//
    //===========================================================//
    // Public Method(s)

    override suspend fun save(portfolio: PortfolioEntity): Result<PortfolioEntity> {
       return runCatching {
           portfolioRepository.save(portfolio)
       }
    }

    //===========================================================//

    override suspend fun getById(portfolioId: UUID): Result<PortfolioEntity> {
        return runCatching {
            portfolioRepository.findWithRelationsById(portfolioId)
                ?: throw PortfolioNotFoundException(portfolioId)
        }
    }
}