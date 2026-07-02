package infrastructure.network.finnhub

import domain.assets.security.SecurityIdentifier
import infrastructure.network.IMarketDataProvider

class FinnhubTester {
    private val m_Provider = IMarketDataProvider.create(IMarketDataProvider.Type.Finnhub)

    fun runFinnhubTester(identifier: SecurityIdentifier) {
        try {
            val quote = m_Provider.getQuote(identifier)
            println("Current Price: " + quote.currentPrice)
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }
}