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
    override suspend fun createPortfolio(userId: UUID): Portfolio {
        val portfolio = Portfolio()
        return portfolioRepository.create(userId, portfolio).getOrThrow()
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

    //===========================================================//

    @Transactional(readOnly = true)
    override suspend fun getPortfolioAccountSummary(userId: UUID): PortfolioAccountSummary {
        val portfolio = portfolioRepository.getByUserId(userId).getOrThrow()

        val summary = accountSummaryProvider.get(AccountSummaryProvider.Type.Ibkr).getAccountSummary()
        val traderCapital = portfolio.traders.sumOf{trader-> trader.capital }

       return PortfolioAccountSummary(
           availableCapital = (summary.availableCapital - traderCapital).coerceAtLeast(0.0),
           netLiquidation = summary.netLiquidation,
       )
    }
}