package data.network

import data.network.finnhub.FinnhubClient
import data.network.finnhub.FinnhubConfig
import data.network.finnhub.FinnhubMarketDataProvider
import data.network.ibkr.IbkrMarketDataProvider
import domain.interfaces.IMarketDataProvider
import infrastructure.broker.IbkrClient

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
            Type.Ibkr -> {
                IbkrMarketDataProvider(IbkrClient())
            }
        }
    }

    //===========================================================//
    //===========================================================//
    // Helper Class(es)

    sealed interface Type {
        data object Finnhub : Type
        data object Ibkr: Type
    }
}