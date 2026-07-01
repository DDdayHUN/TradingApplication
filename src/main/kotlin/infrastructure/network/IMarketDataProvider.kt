package infrastructure.network

import domain.stock.Quote

//===========================================================//
/**
 * Provides market data to the application
 * Implementations can load data from Finnhub
 */
//===========================================================//
interface IMarketDataProvider {
    fun getQuote(symbol: String): Quote
}