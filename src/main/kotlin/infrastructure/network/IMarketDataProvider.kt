package infrastructure.network

import domain.assets.Quote
import domain.assets.security.SecurityIdentifier
import infrastructure.network.finnhub.FinnhubClient
import infrastructure.network.finnhub.FinnhubConfig
import infrastructure.network.finnhub.FinnhubMarketDataProvider
import kotlin.reflect.KClass

//===========================================================//
/**
 * Provides market data to the application.
 */
//===========================================================//

interface IMarketDataProvider {
    fun getQuote(identifier: SecurityIdentifier): Quote

    companion object {
        fun create(type: Type): IMarketDataProvider {
            return when (type) {
                Type.Finnhub -> {
                    FinnhubMarketDataProvider(FinnhubClient(FinnhubConfig()))
                }
            }
        }
    }

    //===========================================================//
    //===========================================================//
    // Helper Class(es)

    sealed interface Type {
        data object Finnhub : Type
    }
}