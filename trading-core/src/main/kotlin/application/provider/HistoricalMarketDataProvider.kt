package application.provider

import application.service.borker.InteractiveBrokersService
import data.repository.historical_data.ibkr.IbkrHistoricalMarketDataProvider
import data.repository.historical_data.yahoo.YahooHistoricalMarketDataRepository
import domain.interfaces.IHistoricalMarketDataProvider
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
            is Type.IbkrHistoricalMarketDataProvider -> {
                IbkrHistoricalMarketDataProvider(type.service)
            }
        }
    }

    //===========================================================//
    //===========================================================//
    // Helper Class(es)

    sealed interface Type {
        data object YahooHistoricalMarketDataRepository : Type
        data class IbkrHistoricalMarketDataProvider(val service: InteractiveBrokersService) : Type
    }
}