package application.service.spring

import application.service.IAuthenticationService
import application.service.IPortfolioService
import domain.Portfolio
import domain.interfaces.IPortfolioRepository
import exception.api.PortfolioNotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

//===========================================================//
//===========================================================//

@Service
class PortfolioService(
    private val session: IAuthenticationService,
    private val portfolioRepository: IPortfolioRepository
) : IPortfolioService {
    //===========================================================//
    //===========================================================//
    // Public Method(s)

    @Transactional
    override suspend fun save(portfolio: Portfolio): Portfolio {
        val user = session.currentUser()
        return portfolioRepository.save(user, portfolio).getOrThrow()
    }

    @Transactional
    override suspend fun createPortfolio(): Portfolio {
        val user = session.currentUser()
        val portfolio = Portfolio()
        return portfolioRepository.save(user, portfolio).getOrThrow()
    }

    //===========================================================//

    @Transactional(readOnly = true)
    override suspend fun getAllPortfolio(): List<Portfolio> {
        val user = session.currentUser()
        return portfolioRepository.getAllByUser(user).getOrThrow()
    }

    //===========================================================//

    @Transactional(readOnly = true)
    override suspend fun getPortfolio(id: UUID): Portfolio {
        val user = session.currentUser()
        return  portfolioRepository.getByIdForUser(user, id).getOrThrow()
    }

    //===========================================================//

    @Transactional
    override suspend fun deleteAllPortfolio(): Boolean {
        TODO("Not yet implemented")
    }

    //===========================================================//

    @Transactional
    override suspend fun deletePortfolio(id: UUID): Boolean {
        TODO("Not yet implemented")
    }
}