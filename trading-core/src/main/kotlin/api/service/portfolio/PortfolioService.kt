package api.service.portfolio

import api.service.auth.IAuthenticationService
import data.repository.portfolio.IPortfolioRepository
import data.repository.portfolio.sql.toDomain
import data.repository.portfolio.sql.toEntity
import data.repository.user.IUserRepository
import data.repository.user.sql.toEntity
import domain.Portfolio
import exception.api.NoPortfolioExistsForUserException
import exception.api.PortfolioAlreadyExistsException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
class PortfolioService(
    private val authService: IAuthenticationService,
    private val portfolioRepository: IPortfolioRepository,
    private val userRepository: IUserRepository
) : IPortfolioService {
    //===========================================================//
    //===========================================================//
    // Public Method(s)

    @Transactional
    override suspend fun update(portfolio: Portfolio): Portfolio {
        // TODO : Itt a sok transzformáció helyett, hogyha majd esetleg kéne optimalizálni, akkor yeaaah.
        val user = authService.currentUser()
        if(user.portfolio == null) throw NoPortfolioExistsForUserException(user.id)

        val userEntity = user.toEntity()
        val portfolioEntity = portfolio.toEntity()

        userEntity.portfolio = portfolioEntity
        portfolioEntity.user = userEntity

        userRepository.save(userEntity)

        return portfolioRepository.save(portfolioEntity).getOrThrow().toDomain()
    }

    //===========================================================//

    @Transactional
    override suspend fun createNewPortfolio(): Portfolio {
        val user = authService.currentUser()
        if(user.portfolio != null) throw PortfolioAlreadyExistsException(user.portfolio.id) // Mivel 1 elemű a Set

        val portfolio = Portfolio()

        val userEntity = user.toEntity()
        val portfolioEntity = portfolio.toEntity()

        userEntity.portfolio = portfolioEntity
        portfolioEntity.user = userEntity

        userRepository.save(userEntity)

        return portfolioRepository.save(portfolioEntity).getOrThrow().toDomain()
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