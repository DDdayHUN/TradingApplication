package application.tester

import domain.assets.security.SecurityHolding
import data.repository.trader.FakeTraderRepository
import domain.trader.Trader
import infrastructure.network.MarketDataProvider

class TraderTester {
    //===========================================================//
    //===========================================================//
    // Private Field(s)

    private val m_TraderRepository = FakeTraderRepository
    private val m_MarketDataProvider = MarketDataProvider.create(MarketDataProvider.Type.Finnhub)

    val trader: Trader

    //===========================================================//
    //===========================================================//
    // Public Method(es)

    suspend fun runTest() {
        println("#================================================#")
        println("# Trader Testing | Algorithm: ${trader.algorithm}")
        println("#================================================#")
        runInternal(trader)
        println("# Trader after save and load")
        println("")
        m_TraderRepository.save(trader)
    }

    //===========================================================//
    //===========================================================//
    // Private Method(es)

    private suspend fun runInternal(trader: Trader) {
        val capitalBeforeOrder = trader.capital
        val holdingsBeforeOrder = trader.holdings

        val quote = m_MarketDataProvider.getQuote(trader.securityIdentifier)

        val order = trader.createOrder(quote)
        trader.finalizeOrder(order)

        println("Trader: ${trader.securityIdentifier.name}")
        println("UUID: ${trader.uuid}")
        println("ISIN: ${trader.securityIdentifier.isin}")
        println("Currency: ${trader.securityIdentifier.currency}")
        println("Current price: ${quote.currentPrice}")
        println()

        println("Capital before order: $capitalBeforeOrder")
        println("Capital after order: ${trader.capital}")
        println()

        println("Order: ${order.toReadableText()}")
        println()

        print("Holdings before order:")
        printHoldings(holdingsBeforeOrder)

        print("Holdings after order:")
        printHoldings(trader.holdings)

        println("#================================================#")
    }

    //===========================================================//

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
        this.trader = trader
    }
}