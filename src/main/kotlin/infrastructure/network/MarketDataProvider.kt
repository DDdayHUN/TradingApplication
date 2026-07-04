package infrastructure.network

import infrastructure.network.finnhub.FinnhubClient
import infrastructure.network.finnhub.FinnhubConfig
import infrastructure.network.finnhub.FinnhubMarketDataProvider

object MarketDataProvider {
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