package application.service.portfolio

import application.provider.AccountSummaryProvider
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
    private val accountSummaryProvider: AccountSummaryProvider,
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
    override suspend fun getPortfolio(): Portfolio {
        return portfolioRepository.getByUserId(authService.currentUser().id).getOrThrow()
    }

    //===========================================================//

    @Transactional
    override suspend fun deletePortfolio(): Boolean {
        TODO("Not yet implemented")
    }

    //===========================================================//

    @Transactional(readOnly = true)
    override suspend fun getPortfolioAccountSummary(): PortfolioAccountSummary {
        val portfolio = portfolioRepository.getByUserId(authService.currentUser().id).getOrThrow()

        val summary = accountSummaryProvider.get(AccountSummaryProvider.Type.Ibkr).getAccountSummary()
        val traderCapital = portfolio.traders.sumOf{trader-> trader.capital }

       return PortfolioAccountSummary(
           availableCapital = (summary.availableCapital - traderCapital).coerceAtLeast(0.0),
           netLiquidation = summary.netLiquidation,
       )
    }
}