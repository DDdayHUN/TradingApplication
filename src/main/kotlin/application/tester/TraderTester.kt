package application.tester

import domain.market.security.SecurityHolding
import data.repository.trader.FakeTraderRepository
import domain.interfaces.ITraderRepository
import domain.trader.Trader
import domain.interfaces.IMarketDataProvider
import data.network.MarketDataProvider

class TraderTester {
    //===========================================================//
    //===========================================================//
    // Private Field(s)

    private val m_TraderRepository: ITraderRepository
    private val m_MarketDataProvider: IMarketDataProvider

    private val m_Trader: Trader

    //===========================================================//
    //===========================================================//
    // Public Method(es)

    suspend fun runTest() {
        println("#================================================#")
        println("# Trader Testing | Algorithm: ${m_Trader.algorithm}")
        println("#================================================#")
        val capitalBeforeOrder = m_Trader.capital
        val holdingsBeforeOrder = m_Trader.holdings

        val quote = m_MarketDataProvider.getQuote(m_Trader.securityIdentifier)

        val order = m_Trader.createOrder(quote)
        m_Trader.finalizeOrder(order)

        println("Trader: ${m_Trader.securityIdentifier.tickerSymbol}")
        println("UUID: ${m_Trader.uuid}")
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
        println("# Trader after save")
        println("")
        m_TraderRepository.save(m_Trader)
    }

    //===========================================================//
    //===========================================================//
    // Private Method(es)

    private fun printHoldings(holdings: List<SecurityHolding>) {
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

    constructor(trader: Trader){
        m_Trader = trader
    }

    //===========================================================//
    //===========================================================//
    // Init

    init{
        m_TraderRepository = FakeTraderRepository
        m_MarketDataProvider = MarketDataProvider.create(MarketDataProvider.Type.Finnhub)
    }
}