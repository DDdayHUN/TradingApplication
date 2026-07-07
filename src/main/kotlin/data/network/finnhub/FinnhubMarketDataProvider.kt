package data.network.finnhub

import domain.market.Quote
import domain.market.security.SecurityIdentifier
import domain.interfaces.IMarketDataProvider

//===========================================================//
/**
 * Provider implementation that gets quote data from Finnhub.
 */
//===========================================================//
class FinnhubMarketDataProvider(
    private val client: FinnhubClient
) : IMarketDataProvider {
    override suspend fun getQuote(identifier: SecurityIdentifier): Quote {
        return client.getQuoteAsync(identifier)
            .getOrThrow()
            .toDomain()
    }
}
