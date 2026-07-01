package infrastructure.network

import domain.assets.Quote
import domain.assets.security.SecurityIdentifier
import infrastructure.network.finnhub.FinnhubMarketDataProvider

//===========================================================//
/**
 * Provides market data to the application.
 */
//===========================================================//

interface IMarketDataProvider {
    fun getQuote(identifier: SecurityIdentifier): Quote
}