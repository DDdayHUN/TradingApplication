package infrastructure.network.finnhub

import domain.assets.Quote
import domain.assets.security.SecurityIdentifier
import infrastructure.network.IMarketDataProvider

//===========================================================//
/**
 * Provider implementation that gets quote data from Finnhub.
 */
//===========================================================//
class FinnhubMarketDataProvider(
    private val client: FinnhubClient
) : IMarketDataProvider {
    override fun getQuote(identifier: SecurityIdentifier): Quote {
        return client.getQuoteAsync(identifier)
            .getOrThrow()
            .toDomain()
    }
}
