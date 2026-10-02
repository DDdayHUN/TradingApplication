package data.network.ibkr

import application.model.User
import application.service.broker.InteractiveBrokersService
import data.network.IAccountSummaryProvider
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class IbkrAccountSummaryProvider(
    private val ibkrService: InteractiveBrokersService
): IAccountSummaryProvider {
    //===========================================================//
    //===========================================================//
    // Public Method(s)

    override suspend fun getAccountSummary(userId: UUID): User.AccountSummary {
        val summary = ibkrService.getAccountSummary(userId)
        return User.AccountSummary(
            availableCapital = summary.availableCapital,
            netLiquidation = summary.netLiquidation,
        )
    }
}