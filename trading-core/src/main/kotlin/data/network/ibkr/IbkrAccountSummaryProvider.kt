package data.network.ibkr

import application.model.User
import application.service.broker.InteractiveBrokersService
import data.network.IAccountSummaryProvider
import org.springframework.stereotype.Component

@Component
class IbkrAccountSummaryProvider(
    private val ibkrService: InteractiveBrokersService
): IAccountSummaryProvider {
    //===========================================================//
    //===========================================================//
    // Public Method(s)

    override suspend fun getAccountSummary(): User.AccountSummary {
        val summary = ibkrService.getAccountSummary()
        return User.AccountSummary(
            availableCapital = summary.availableCapital,
            netLiquidation = summary.netLiquidation,
        )
    }
}