package infrastructure.network.finnhub

import domain.assets.security.SecurityIdentifier
import java.io.IOException

class FinnhubTester {
    private val m_config: FinnhubConfig
    private val m_client: FinnhubClient
    private val m_provider: FinnhubMarketDataProvider

    @Throws(IOException::class, InterruptedException::class)
    fun runFinnhubTester(identifier: SecurityIdentifier) {
        try {
            val quote = m_provider.getQuote(identifier)

            println("Symbol: " + "identifier.tickerSymbol")
            println("Current Price: " + quote.currentPrice)
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    init {
        this.m_config = FinnhubConfig()
        this.m_client = FinnhubClient(m_config)
        this.m_provider = FinnhubMarketDataProvider(this.m_client)
    }
}