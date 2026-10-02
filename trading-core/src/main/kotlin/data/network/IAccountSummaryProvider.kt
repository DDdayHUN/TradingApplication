package data.network

import domain.Portfolio.PortfolioAccountSummary
import java.util.UUID

interface IAccountSummaryProvider {
    suspend fun getAccountSummary(userId: UUID): PortfolioAccountSummary
}