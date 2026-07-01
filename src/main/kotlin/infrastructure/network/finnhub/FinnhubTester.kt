package infrastructure.network.finnhub

import java.io.IOException

class FinnhubTester {
    private val m_config: FinnhubConfig
    private val m_client: FinnhubClient
    private val m_provider: FinnhubMarketDataProvider

    @Throws(IOException::class, InterruptedException::class)
    fun runFinnhubTester(symbol: String) {
        try {
            val quote = m_provider.getQuote(symbol)

            println("Symbol: " + quote.symbol)
            println("Current Price: " + quote.currentPrice)
            println("Change: " + quote.change)
            println("Percent Change: " + quote.percentChange + "%")
            println("High: " + quote.highPrice)
            println("Low: " + quote.lowPrice)
            println("Open: " + quote.openPrice)
            println("Previous Close: " + quote.prevClosePrice)
            println("Received at: " + quote.formattedReceivedAt)
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