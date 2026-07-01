package infrastructure.network.finnhub

import domain.assets.security.SecurityIdentifier

class FinnhubTester {
    private val m_Config: FinnhubConfig = FinnhubConfig()
    private val m_Client: FinnhubClient = FinnhubClient(this.m_Config)
    private val m_Provider: FinnhubMarketDataProvider = FinnhubMarketDataProvider(this.m_Client)

    fun runFinnhubTester(identifier: SecurityIdentifier) {
        try {
            val quote = m_Provider.getQuote(identifier)
            println("Current Price: " + quote.currentPrice)
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }
}