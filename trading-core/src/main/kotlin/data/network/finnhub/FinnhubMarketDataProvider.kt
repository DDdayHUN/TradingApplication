package data.network.finnhub

import data.network.IMarketDataProvider
import domain.market.Quote
import domain.market.security.SecurityIdentifier
import org.springframework.stereotype.Component
import java.util.UUID

//===========================================================//
/**
 * Provider implementation that gets quote data from Finnhub.
 */
//===========================================================//
@Component
class FinnhubMarketDataProvider(
    private val client: FinnhubClient
) : IMarketDataProvider {
    //===========================================================//
    //===========================================================//
    // Public Method(s)

    // TODO : Itt nincsen használva a UserId?
    override suspend fun getQuote(userId: UUID, identifier: SecurityIdentifier): Result<Quote> {
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
