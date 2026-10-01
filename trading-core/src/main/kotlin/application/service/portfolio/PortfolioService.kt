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
    override suspend fun create(): Portfolio {
        val userId = authService.currentUser().id
        val portfolio = Portfolio()
        return portfolioRepository.create(userId, portfolio).getOrThrow()
    }

    //===========================================================//

    @Transactional(readOnly = true)
    override suspend fun getAll(): List<Portfolio> {
        val userId = authService.currentUser().id
        return portfolioRepository.getAllByUserId(userId).getOrThrow()
    }

    //===========================================================//

    @Transactional(readOnly = true)
    override suspend fun getById(portfolioId: UUID): Portfolio {
        return portfolioRepository.getById(portfolioId).getOrThrow()
    }

    //===========================================================//

    @Transactional
    override suspend fun delete(portfolioId: UUID): Boolean {
        TODO("Not yet implemented")
    }
}