package data.repository.historical_data

import domain.assets.security.SecurityIdentifier

internal interface IHistoricalMarketDataRepository {
    suspend fun getBySecurityIdentifier(securityIdentifier: SecurityIdentifier): HistoricalMarketDataDto
    suspend fun getAll(): List<HistoricalMarketDataDto>
}