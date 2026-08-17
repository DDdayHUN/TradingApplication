package data.repository.portfolio

import data.repository.user.toEntity
import domain.Portfolio
import domain.User
import domain.interfaces.IPortfolioRepository
import domain.interfaces.IUserRepository
import exception.api.PortfolioNotFoundException
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
class PortfolioRepository(
    private val userRepository : IUserRepository,
    private val portfolioRepository: IPortfolioJpaRepository
) : IPortfolioRepository {

    override suspend fun save(user: User, portfolio: Portfolio): Result<Portfolio> {
       return runCatching {
           val user = userRepository.getById(user.id).getOrThrow()
           portfolioRepository.save(portfolio.toEntity(user.toEntity())).toDomain()
       }
    }

    override suspend fun getById(id: UUID): Result<Portfolio> {
        return runCatching {
            val entity = portfolioRepository.findPortfolioWithTradersById(id)
                ?: throw PortfolioNotFoundException(id)

            entity.toDomain()
        }
    }

    override suspend fun getAllByUser(user: User): Result<List<Portfolio>> {
        return runCatching {
            portfolioRepository.findAllByUserId(user.id)
                .map {portfolio ->
                    portfolio.toDomain()
                }
        }
    }
}