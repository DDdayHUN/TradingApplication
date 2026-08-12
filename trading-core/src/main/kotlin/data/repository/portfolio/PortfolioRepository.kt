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
    private val mapper : PortfolioMapper

    override suspend fun save(portfolio: Portfolio): Result<Unit> {
       return runCatching {
           val userEntity = userRepository.findById(portfolio.userId)
               .orElseThrow { UserNotFoundException(portfolio.userId) }

           val entity = mapper.toEntity(
               portfolio = portfolio,
               userEntity = userEntity
           )

           repository.save(entity)
       }
    }

    override suspend fun getById(id: UUID): Result<Portfolio> {
        return runCatching {
            val entity = repository.findById(id)
                .orElseThrow { PortfolioNotFoundException(id) }

            mapper.toDomain(entity)
        }
    }

    override suspend fun getAll(): Result<List<Portfolio>> {
        return runCatching {
             repository.findAll().map(mapper::toDomain)
        }
    }

    constructor(repository : IPortfolioJpaRepository, userRepository: IUserJpaRepository, mapper : PortfolioMapper) {
        this.repository = repository
        this.userRepository = userRepository
        this.mapper = mapper

    }

}