package application.provider

import data.network.IMarketDataProvider
import data.network.finnhub.FinnhubMarketDataProvider
import data.network.ibkr.IbkrMarketDataProvider
import org.springframework.stereotype.Component

@Component
class MarketDataProvider(
    private val finnhubProvider: FinnhubMarketDataProvider,
    private val ibkrProvider: IbkrMarketDataProvider
) {
    fun get(type: Type): IMarketDataProvider {
        return when (type) {
            Type.Finnhub -> finnhubProvider
            Type.Ibkr -> ibkrProvider
        }
    }

    //===========================================================//
    //===========================================================//
    // Helper Class(es)

    sealed interface Type {
        data object Finnhub: Type
        data object Ibkr: Type
    }
}