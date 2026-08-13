package data.repository.portfolio

import data.repository.user.IUserJpaRepository
import domain.Portfolio
import domain.interfaces.IPortfolioRepository
import exception.api.PortfolioNotFoundException
import exception.api.UserNotFoundException
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
class PortfolioRepository : IPortfolioRepository {

    private val repository: IPortfolioJpaRepository
    private val userRepository : IUserJpaRepository

    override suspend fun save(portfolio: Portfolio): Result<Unit> {
       return runCatching {
           val userEntity = userRepository.findById(portfolio.userId)
               .orElseThrow { UserNotFoundException(portfolio.userId) }

           repository.save(portfolio.toEntity(userEntity))
       }
    }

    override suspend fun getById(id: UUID): Result<Portfolio> {
        return runCatching {
            val entity = repository.findPortfolioWithTradersById(id)
                ?: throw PortfolioNotFoundException(id)

            entity.toDomain()
        }
    }

    override suspend fun getAllByUserId(userId: UUID): Result<List<Portfolio>> {
        return runCatching {
            repository.findAllByUserId(userId)
                .map {portfolio ->
                    portfolio.toDomain()
                }
        }
    }

    override suspend fun getAll(): Result<List<Portfolio>> {
        return runCatching {
             repository.findAll().map{ portfolio ->
                 portfolio.toDomain()
             }
        }
    }

    constructor(repository : IPortfolioJpaRepository, userRepository: IUserJpaRepository) {
        this.repository = repository
        this.userRepository = userRepository
    }

}