package data.repository.historical_data

import domain.assets.security.SecurityIdentifier

internal interface IHistoricalMarketDataRepository {
    fun getById(securityIdentifier: SecurityIdentifier): HistoricalMarketData
}