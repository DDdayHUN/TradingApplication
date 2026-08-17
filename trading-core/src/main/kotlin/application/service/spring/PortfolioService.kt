package application.service.spring

import application.service.IAuthenticationService
import application.service.IPortfolioService
import domain.Portfolio
import domain.interfaces.IPortfolioRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

//===========================================================//
//===========================================================//

@Service
class PortfolioService(
    private val session: IAuthenticationService,
    private val portfolioRepository: IPortfolioRepository,
) : IPortfolioService {
    //===========================================================//
    //===========================================================//
    // Public Method(s)

    @Transactional
    override suspend fun createPortfolio(capital: Double): Portfolio {
        require(capital >= 0.0) {
            "Portfolio capital must be greater or equal to zero"
        }

        val user = session.currentUser()
        val portfolio = Portfolio(
            capital = capital
        )

        return portfolioRepository.save(user, portfolio).getOrThrow()
    }

    //===========================================================//

    @Transactional(readOnly = true)
    override suspend fun getAllPortfolio(): List<Portfolio> {
        val user = session.currentUser()
        return portfolioRepository.getAllByUser(user).getOrThrow()
    }
}