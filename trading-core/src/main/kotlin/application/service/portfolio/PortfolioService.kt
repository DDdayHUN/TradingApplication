package application.service.portfolio

import application.service.user.IUserService
import data.repository.portfolio.IPortfolioRepository
import data.repository.portfolio.sql.toDomain
import data.repository.portfolio.sql.toEntity
import data.repository.portfolio.sql.update
import data.repository.user.sql.toDomain
import data.repository.user.sql.toEntity
import domain.Portfolio
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
class PortfolioService(
    private val portfolioRepository: IPortfolioRepository,
    private val userService: IUserService,
) : IPortfolioService {
    //===========================================================//
    //===========================================================//
    // Public Method(s)

    @Transactional
    override suspend fun update(userId: UUID, portfolio: Portfolio): Portfolio {
        val user = userService.getById(userId)
        val portfolioEntity = portfolioRepository.getById(portfolio.id).getOrThrow()

        require(portfolioEntity.id == user.portfolio.id) { "ID mismatch. ID1 {${portfolioEntity.id}} ID2 {${user.portfolio.id}}" }

        portfolioEntity.update(portfolio)
        return portfolioRepository.save(portfolioEntity).getOrThrow().toDomain()
    }

    //===========================================================//

    @Transactional
    override suspend fun create(userId: UUID): Portfolio {
        val user = userService.getById(userId)
        val userEntity = user.toEntity()

        val portfolio = Portfolio()
        val portfolioEntity = portfolio.toEntity()

        userEntity.portfolio = portfolioEntity
        portfolioEntity.user = userEntity

        userService.update(userEntity.toDomain())
        return portfolioRepository.save(portfolioEntity).getOrThrow().toDomain()
    }

    //===========================================================//

    @Transactional(readOnly = true)
    override suspend fun get(userId: UUID): Portfolio {
        val user = userService.getById(userId)
        return user.portfolio
    }
}