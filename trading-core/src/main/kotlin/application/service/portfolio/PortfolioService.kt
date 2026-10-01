package application.service.portfolio

import application.service.auth.IAuthenticationService
import data.repository.portfolio.IPortfolioRepository
import domain.Portfolio
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
class PortfolioService(
    private val authService: IAuthenticationService,
    private val portfolioRepository: IPortfolioRepository,
) : IPortfolioService {
    //===========================================================//
    //===========================================================//
    // Public Method(s)

    @Transactional
    override suspend fun save(portfolio: Portfolio): Portfolio {
        return portfolioRepository.save(portfolio).getOrThrow()
    }

    //===========================================================//

    @Transactional
    override suspend fun createPortfolio(userId: UUID): Portfolio {
        val user = authService.currentUser()
        val portfolio = Portfolio()
        return portfolioRepository.create(user.id, portfolio).getOrThrow()
    }

    //===========================================================//

    @Transactional(readOnly = true)
    override suspend fun getPortfolioByUserId(userId: UUID): Portfolio {
        return portfolioRepository.getByUserId(userId).getOrThrow()
    }

    //===========================================================//

    @Transactional
    override suspend fun deletePortfolio(userId: UUID): Boolean {
        TODO("Not yet implemented")
    }
}