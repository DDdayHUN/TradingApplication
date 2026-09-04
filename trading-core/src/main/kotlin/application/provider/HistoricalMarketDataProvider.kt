package application.provider

import data.repository.historical_data.json.yahoo.YahooHistoricalMarketDataRepository
import data.repository.historical_data.IHistoricalMarketDataProvider
import org.springframework.stereotype.Component

@Component
object HistoricalMarketDataProvider {
    //===========================================================//
    //===========================================================//
    // Public Method(es)

    fun get(type: Type): IHistoricalMarketDataProvider {
        return when (type) {
            is Type.YahooHistoricalMarketDataRepository -> {
                YahooHistoricalMarketDataRepository
            }
        }
    }

    //===========================================================//
    //===========================================================//
    // Helper Class(es)

    sealed interface Type {
        data object YahooHistoricalMarketDataRepository : Type
    }
}