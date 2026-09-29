package data.network.ibkr

import application.service.broker.InteractiveBrokersService
import data.network.IAccountSummaryProvider
import domain.Portfolio.PortfolioAccountSummary
import org.springframework.stereotype.Component

@Component
class IbkrAccountSummaryProvider(
    private val ibkrService: InteractiveBrokersService
): IAccountSummaryProvider {
    override suspend fun getAccountSummary(): PortfolioAccountSummary {
        val summary = ibkrService.getAccountSummary()
        return PortfolioAccountSummary(
            availableCapital = summary.availableCapital,
            netLiquidation = summary.netLiquidation,
        )
    }
}