package application.service.portfolio

import data.repository.portfolio.IPortfolioRepository
import data.repository.portfolio.sql.toDomain
import data.repository.portfolio.sql.toEntity
import data.repository.portfolio.sql.update
import data.repository.user.IUserRepository
import data.repository.user.sql.toDomain
import domain.Portfolio
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
class PortfolioService(
    private val portfolioRepository: IPortfolioRepository,
    private val userRepository: IUserRepository,
) : IPortfolioService {
    //===========================================================//
    //===========================================================//
    // Public Method(s)

    @Transactional
    override suspend fun update(userId: UUID, portfolio: Portfolio): Portfolio {
        val userEntity = userRepository.getById(userId).getOrThrow()
        val portfolioEntity = portfolioRepository.getById(portfolio.id).getOrThrow()

        require(portfolioEntity.id == userEntity.portfolio.id) { "ID mismatch. ID1 {${portfolioEntity.id}} ID2 {${userEntity.portfolio.id}}" }

        portfolioEntity.update(portfolio)
        return portfolioRepository.save(portfolioEntity).getOrThrow().toDomain()
    }

    //===========================================================//

    @Transactional
    override suspend fun create(userId: UUID): Portfolio {
        val userEntity = userRepository.getById(userId).getOrThrow()

        val portfolio = Portfolio()
        val portfolioEntity = portfolio.toEntity()

        userEntity.portfolio = portfolioEntity
        portfolioEntity.user = userEntity

        userRepository.save(userEntity)
        return portfolioRepository.save(portfolioEntity).getOrThrow().toDomain()
    }

    //===========================================================//

    @Transactional(readOnly = true)
    override suspend fun get(userId: UUID): Portfolio {
        val user = userRepository.getById(userId).getOrThrow().toDomain()
        return user.portfolio
    }
}