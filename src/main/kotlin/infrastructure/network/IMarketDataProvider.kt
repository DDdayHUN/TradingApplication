package infrastructure.network

import domain.assets.Quote
import domain.assets.security.SecurityIdentifier
import infrastructure.network.finnhub.FinnhubClient
import infrastructure.network.finnhub.FinnhubMarketDataProvider

//===========================================================//
/**
 * Provides market data to the application.
 */
//===========================================================//

interface IMarketDataProvider {

    companion object {
        fun FINNHUB(client: FinnhubClient): IMarketDataProvider {
            return FinnhubMarketDataProvider(client)
        }
    }

    fun getQuote(identifier: SecurityIdentifier): Quote
}