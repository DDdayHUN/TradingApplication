package data.network.ibkr

import application.service.broker.InteractiveBrokersService
import data.network.IAccountSummaryProvider
import domain.Portfolio.PortfolioAccountSummary
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class IbkrAccountSummaryProvider(
    private val ibkrService: InteractiveBrokersService
): IAccountSummaryProvider {
    //===========================================================//
    //===========================================================//
    // Public Method(s)

    override suspend fun getAccountSummary(userId: UUID): PortfolioAccountSummary {
        val summary = ibkrService.getAccountSummary(userId)
        return PortfolioAccountSummary(
            availableCapital = summary.availableCapital,
            netLiquidation = summary.netLiquidation,
        )
    }
}