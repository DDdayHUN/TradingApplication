package data.repository.historical_data

import domain.market.security.SecurityIdentifier

internal interface IHistoricalMarketDataRepository {
    suspend fun getBySecurityIdentifier(securityIdentifier: SecurityIdentifier): HistoricalMarketDataDto
    suspend fun getAll(): List<HistoricalMarketDataDto>
}