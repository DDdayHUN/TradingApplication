/*package application.tester

import data.network.MarketDataProvider
import data.network.finnhub.FinnhubConfig
import data.repository.trader.FakeTraderRepository
import domain.interfaces.IMarketDataProvider
import domain.interfaces.ITraderRepository
import domain.market.security.SecurityHolding
import domain.trader.Trader

//===========================================================//
/**
 * Test utility for executing a single trading cycle with a trader instance.
 *
 * The tester retrieves the latest market quote. Creates, finalizes and prints a
 * trading order, the trader's state before and after executing said order,
 * and saves the updated trader to the configured repository.
 */
//===========================================================//

class TraderTester {
    //===========================================================//
    //===========================================================//
    // Private Field(s)

    private val m_TraderRepository: ITraderRepository
    private val m_MarketDataProvider: IMarketDataProvider

    private val m_Trader: Trader
    private val m_finnhubConfig: FinnhubConfig

    //===========================================================//
    //===========================================================//
    // Public Method(es)

    suspend fun runTest() {
        println("#================================================#")
        println("# Trader Testing | Algorithm: ${m_Trader.algorithm}")
        println("#================================================#")
        val capitalBeforeOrder = m_Trader.capital
        val holdingsBeforeOrder = m_Trader.holdings

        val quote = m_MarketDataProvider.getQuote(m_Trader.securityIdentifier).getOrThrow()

        val order = m_Trader.createOrder(quote)
        m_Trader.finalizeOrder(order)

        println("Trader: ${m_Trader.securityIdentifier.tickerSymbol}")
        println("UUID: ${m_Trader.id}")
        println("ISIN: ${m_Trader.securityIdentifier.isin}")
        println("Currency: ${m_Trader.securityIdentifier.currency}")
        println("Current price: ${quote.currentPrice}")
        println()

        println("Capital before order: $capitalBeforeOrder")
        println("Capital after order: ${m_Trader.capital}")
        println()

        println("Order: ${order.toReadableText()}")
        println()

        print("Holdings before order:")
        printHoldings(holdingsBeforeOrder)

        print("Holdings after order:")
        printHoldings(m_Trader.holdings)

        println("#================================================#")
        println("# Save Trader: ${m_Trader.id}")
        println("")
        m_TraderRepository.save(m_Trader).getOrThrow()
    }

    //===========================================================//
    //===========================================================//
    // Private Method(es)

    private fun printHoldings(holdings: Set<SecurityHolding>) {
        if (holdings.isEmpty()) {
            print("None")
            println()
            return
        }

        println()
        holdings.forEach { holding ->
            println(holding.toString())
        }
    }

    //===========================================================//
    //===========================================================//
    // Constructor(s)

    constructor(trader: Trader, finnhubConfig: FinnhubConfig) {
        m_Trader = trader
        m_finnhubConfig = finnhubConfig
    }

    //===========================================================//
    //===========================================================//
    // Init

    init{
        m_TraderRepository = FakeTraderRepository
        m_MarketDataProvider = MarketDataProvider.create(MarketDataProvider.Type.Finnhub(m_finnhubConfig))
    }
}*/