package data.repository.historical_data

import domain.assets.security.SecurityIdentifier

internal interface IHistoricalMarketDataRepository {
    fun getBySecurityIdentifier(securityIdentifier: SecurityIdentifier): HistoricalMarketDataDto
}