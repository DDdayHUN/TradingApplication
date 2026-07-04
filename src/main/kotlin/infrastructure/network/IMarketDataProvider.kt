package infrastructure.network

import domain.assets.security.SecurityIdentifier

//===========================================================//
/**
 * Provides market data to the application.
 */
//===========================================================//

interface IMarketDataProvider {
    suspend fun getQuote(identifier: SecurityIdentifier): Quote
}