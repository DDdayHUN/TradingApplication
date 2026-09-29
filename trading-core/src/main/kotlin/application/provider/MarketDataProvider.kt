package application.provider

import data.network.IMarketDataProvider
import data.network.finnhub.FinnhubMarketDataProvider
import data.network.ibkr.IbkrMarketDataProvider
import org.springframework.beans.factory.ObjectProvider
import org.springframework.stereotype.Component

@Component
class MarketDataProvider(
    private val finnhubProvider: ObjectProvider<FinnhubMarketDataProvider>,
    private val ibkrProvider: ObjectProvider<IbkrMarketDataProvider>
) {
    fun get(type: Type): IMarketDataProvider {
        return when (type) {
            Type.Finnhub -> finnhubProvider.getObject()
            Type.Ibkr -> ibkrProvider.getObject()
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