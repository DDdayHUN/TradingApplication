package application.tester

import domain.algorithm.TradingAlgorithm
import domain.assets.security.SecurityHolding
import domain.assets.security.SecurityIdentifier
import data.repository.trader.FakeTraderRepository
import domain.trader.Trader
import infrastructure.network.MarketDataProvider

class TraderTester {
    //===========================================================//
    //===========================================================//
    // Private Field(s)

    private val m_TraderRepository = FakeTraderRepository
    private val m_MarketDataProvider = MarketDataProvider.create(MarketDataProvider.Type.Finnhub)

    private val m_SecurityIdentifier: SecurityIdentifier
    private val m_Holdings: MutableList<SecurityHolding>
    private val m_Capital: Double
    private val m_AlgorithmType: TradingAlgorithm.Type

    //===========================================================//
    //===========================================================//
    // Public Method(es)

    suspend fun runTest() {
        val trader = Trader(
            m_SecurityIdentifier,
            m_Holdings,
            m_Capital,
            TradingAlgorithm.create(m_AlgorithmType, m_SecurityIdentifier)
        )
        println("#================================================#")
        println("# Trader Testing")
        println("#================================================#")
        runInternal(trader)
        println("# Trader after save and load")
        println("")
        m_TraderRepository.save(trader)
        val trader2 = m_TraderRepository.getBySecurityIdentifier(m_SecurityIdentifier)
        runInternal(trader2)
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

    constructor(securityIdentifier: SecurityIdentifier, holdings: MutableList<SecurityHolding>, capital: Double, algorithmType: TradingAlgorithm.Type) {
        m_SecurityIdentifier = securityIdentifier
        m_Holdings = holdings
        m_Capital = capital
        m_AlgorithmType = algorithmType
    }
}