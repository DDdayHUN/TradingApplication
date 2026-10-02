package application.service.portfolio

import application.service.user.IUserService
import data.repository.portfolio.IPortfolioRepository
import data.repository.portfolio.sql.toDomain
import data.repository.portfolio.sql.toEntity
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

        val userEntity = user.toEntity()
        val portfolioEntity = portfolio.toEntity()

        userEntity.portfolio = portfolioEntity
        portfolioEntity.user = userEntity

        return portfolioRepository.save(portfolioEntity).getOrThrow().toDomain()
    }

    //===========================================================//

    @Transactional
    override suspend fun create(userId: UUID): Portfolio {
        val user = userService.getById(userId)

        val portfolio = Portfolio()

        val userEntity = user.toEntity()
        val portfolioEntity = portfolio.toEntity()

        userEntity.portfolio = portfolioEntity
        portfolioEntity.user = userEntity

        return portfolioRepository.save(portfolioEntity).getOrThrow().toDomain()
    }

    @Transactional(readOnly = true)
    override suspend fun get(userId: UUID): Portfolio {
        val user = userService.getById(userId)
        return user.portfolio
    }

    //===========================================================//

    /*
    @Transactional
    override suspend fun deletePortfolio(user: User) {
        if(user.portfolios.isEmpty()) throw NoPortfolioExistsForUserException(user.id)

        TODO("Not yet implemented")
    }
    */
}