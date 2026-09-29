package data.network

import domain.Portfolio.PortfolioAccountSummary

interface IAccountSummaryProvider {
    suspend fun getAccountSummary(): PortfolioAccountSummary
}