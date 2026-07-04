package infrastructure.network.finnhub

import domain.assets.security.SecurityIdentifier
import infrastructure.network.MarketDataProvider

class FinnhubTester {
    private val m_Provider = MarketDataProvider.create(MarketDataProvider.Type.Finnhub)

    suspend fun runFinnhubTester(identifier: SecurityIdentifier) {
        try {
            val quote = m_Provider.getQuote(identifier)
            println("Current Price: " + quote.currentPrice)
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }
}