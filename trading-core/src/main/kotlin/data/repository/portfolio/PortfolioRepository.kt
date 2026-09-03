package data.repository.portfolio

import data.repository.user.toEntity
import domain.Portfolio
import domain.interfaces.IPortfolioRepository
import domain.interfaces.IUserRepository
import exception.api.PortfolioNotFoundException
import org.springframework.stereotype.Repository
import java.util.*

@Repository
class PortfolioRepository(
    private val userRepository : IUserRepository,
    private val portfolioRepository: IPortfolioJpaRepository
) : IPortfolioRepository {

    override suspend fun create(userId: UUID, portfolio: Portfolio): Result<Portfolio> {
        return runCatching {
            val user = userRepository.getById(userId).getOrThrow()
            portfolioRepository.save(portfolio.toEntity(user.toEntity())).toDomain()
        }
    }

    override suspend fun save(portfolio: Portfolio): Result<Portfolio> {
       return runCatching {
           val existingEntity = portfolioRepository
               .findById(portfolio.id)
               .orElseThrow {
                   PortfolioNotFoundException(portfolio.id)
               }

           val updatedEntity = portfolio.toEntity(
               existingEntity.user
           )

           portfolioRepository
               .save(updatedEntity)
               .toDomain()
       }
    }

    override suspend fun getByTraderId(traderId: UUID): Result<Portfolio> {
        return runCatching{
            val portfolio = portfolioRepository.findByTradersId(traderId)
                ?: throw IllegalArgumentException("Portfolio not found for trader with id $traderId")
            portfolio.toDomain()
        }
    }

    override suspend fun getById(id: UUID): Result<Portfolio> {
        return runCatching {
           val portfolio = portfolioRepository.findWithRelationsById(id)
                ?: throw PortfolioNotFoundException(id)

            portfolio.toDomain()
        }
    }

    override suspend fun getByIdForUser(userId: UUID, id: UUID): Result<Portfolio> {
        return runCatching {
            val entity = portfolioRepository.findByUserIdAndId(userId, id)
                ?: throw PortfolioNotFoundException(id)

            entity.toDomain()
        }
    }

    override suspend fun getAllByUserId(userId: UUID): Result<List<Portfolio>> {
        return runCatching {
            portfolioRepository.findAllByUserId(userId)
                .map {portfolio ->
                    portfolio.toDomain()
                }
        }
    }
}