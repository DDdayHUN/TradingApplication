package data.repository.historical_data

import domain.market.security.SecurityIdentifier

interface IHistoricalMarketDataRepository {
    suspend fun getBySecurityIdentifier(securityIdentifier: SecurityIdentifier): HistoricalMarketDataDto
    suspend fun getAll(): List<HistoricalMarketDataDto>
}