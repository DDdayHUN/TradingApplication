package domain.interfaces

import domain.market.Quote
import domain.market.security.SecurityIdentifier

interface IMarketDataProvider {
    suspend fun getQuote(identifier: SecurityIdentifier): Result<Quote>
}