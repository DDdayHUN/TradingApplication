package data.network

import data.network.finnhub.FinnhubClient
import data.network.finnhub.FinnhubConfig
import data.network.finnhub.FinnhubMarketDataProvider
import domain.interfaces.IMarketDataProvider

//===========================================================//
/**
 * Factory object for creating market data provider implementations
 */
//===========================================================//
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