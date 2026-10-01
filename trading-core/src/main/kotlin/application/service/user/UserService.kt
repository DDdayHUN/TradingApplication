package application.service.user

import application.model.AccountSummary
import application.service.auth.IAuthenticationService
import org.springframework.transaction.annotation.Transactional

class UserService(
    private val authService: IAuthenticationService,
) : IUserService {

    @Transactional(readOnly = true)
    override suspend fun getAccountSummary(): AccountSummary {
        TODO("Not yet implemented")
        /*
        val user = authService.currentUser()
        val portfolio = getPortfolio(user.)

        val summary = accountSummaryProvider.get(AccountSummaryProvider.Type.Ibkr).getAccountSummary()
        val traderCapital = portfolio.traders.sumOf{trader-> trader.capital }

        return AccountSummary(
            availableCapital = (summary.availableCapital - traderCapital).coerceAtLeast(0.0),
            netLiquidation = summary.netLiquidation,
        )
         */
    }
}