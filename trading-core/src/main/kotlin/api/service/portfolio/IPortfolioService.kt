package api.service.portfolio

import domain.Portfolio

interface IPortfolioService {
    suspend fun update(portfolio: Portfolio): Portfolio
    suspend fun createNewPortfolio(): Portfolio
    // suspend fun deletePortfolio() Not yet needed
    // suspend fun deleteAllPortfolio() Not yet needed
}