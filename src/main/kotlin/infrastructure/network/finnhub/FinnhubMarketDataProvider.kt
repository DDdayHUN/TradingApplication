package infrastructure.network.finnhub

import domain.stock.Quote
import infrastructure.network.IMarketDataProvider
import java.io.IOException

//===========================================================//
/**
 * Provider implementation that gets quote data from Finnhub.
 */
//===========================================================//
class FinnhubMarketDataProvider(
    private val client: FinnhubClient
) : IMarketDataProvider {
    @Throws(IOException::class, InterruptedException::class)
    override fun getQuote(symbol: String): Quote {
        return client.getQuote(symbol)
    }
}
