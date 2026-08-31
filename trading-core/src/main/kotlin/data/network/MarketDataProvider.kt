package data.network

import data.network.finnhub.FinnhubClient
import data.network.finnhub.FinnhubConfig
import data.network.finnhub.FinnhubMarketDataProvider
import data.network.ibkr.IbkrMarketDataProvider
import domain.interfaces.IMarketDataProvider
import infrastructure.broker.IbkrSession

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
            is Type.Ibkr -> {
                IbkrMarketDataProvider(type.session)
            }
        }
    }

    //===========================================================//
    //===========================================================//
    // Helper Class(es)

    sealed interface Type {
        data object Finnhub : Type
        data class Ibkr(
            val session: IbkrSession
        ): Type
    }
}