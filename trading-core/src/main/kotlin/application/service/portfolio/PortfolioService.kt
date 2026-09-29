package application.service.portfolio

import application.service.auth.IAuthenticationService
import application.service.broker.InteractiveBrokersService
import data.repository.portfolio.IPortfolioRepository
import domain.Portfolio
import domain.Portfolio.PortfolioAccountSummary
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
class PortfolioService(
    private val authService: IAuthenticationService,
    private val ibkrService: InteractiveBrokersService,
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
    override suspend fun createPortfolio(): Portfolio {
        val userId = authService.currentUser().id
        val portfolio = Portfolio()
        return portfolioRepository.create(userId, portfolio).getOrThrow()
    }

    //===========================================================//

    @Transactional(readOnly = true)
    override suspend fun getAllPortfolio(): List<Portfolio> {
        val userId = authService.currentUser().id
        return portfolioRepository.getAllByUserId(userId).getOrThrow()
    }

    //===========================================================//

    @Transactional(readOnly = true)
    override suspend fun getPortfolio(portfolioId: UUID): Portfolio {
        return portfolioRepository.getById(portfolioId).getOrThrow()
    }

    //===========================================================//

    @Transactional
    override suspend fun deletePortfolio(portfolioId: UUID): Boolean {
        TODO("Not yet implemented")
    }

    //===========================================================//

    @Transactional(readOnly = true)
    override suspend fun getPortfolioAccountSummary(portfolioId: UUID): PortfolioAccountSummary {
        val portfolio = getPortfolio(portfolioId)

        val summary = ibkrService.getAccountSummary()

        val traderCapital = portfolio.traders.sumOf{trader->
            trader.capital
        }

       return PortfolioAccountSummary(
           availableCapital = (summary.availableCapital - traderCapital).coerceAtLeast(0.0),
           netLiquidation = summary.netLiquidation,
       )
    }
}