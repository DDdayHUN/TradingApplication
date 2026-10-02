package data.network

import domain.market.Quote
import domain.market.security.SecurityIdentifier
import java.util.UUID

interface IMarketDataProvider {
    suspend fun getQuote(userId: UUID, identifier: SecurityIdentifier): Result<Quote>
}