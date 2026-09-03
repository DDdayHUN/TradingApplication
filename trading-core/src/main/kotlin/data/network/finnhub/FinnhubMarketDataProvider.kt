package data.network.finnhub

import domain.interfaces.IMarketDataProvider
import domain.market.Quote
import domain.market.security.SecurityIdentifier

//===========================================================//
/**
 * Provider implementation that gets quote data from Finnhub.
 */
//===========================================================//
internal class FinnhubMarketDataProvider(
    private val client: FinnhubClient
) : IMarketDataProvider {
    override suspend fun getQuote(identifier: SecurityIdentifier): Result<Quote> {
        try {
            val ret = client.getQuoteAsync(identifier)
                .getOrThrow()
                .toDomain()

            return Result.success(ret)
        }
        catch (e: Exception) {
            return Result.failure(e)
        }
    }
}
