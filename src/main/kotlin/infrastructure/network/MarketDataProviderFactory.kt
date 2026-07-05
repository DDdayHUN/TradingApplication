package infrastructure.network

import infrastructure.network.finnhub.FinnhubClient
import infrastructure.network.finnhub.FinnhubConfig
import infrastructure.network.finnhub.FinnhubMarketDataProvider
//===========================================================//
/**
 * Factory object for creating market data provider implementations
 */
//===========================================================//
object MarketDataProviderFactory {
    fun create(type: Type): IMarketDataProvider {
        return when (type) {
            Type.Finnhub -> {
                FinnhubMarketDataProvider(FinnhubClient(FinnhubConfig()))
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