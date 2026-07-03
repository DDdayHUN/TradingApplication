package application.tester

import domain.algorithm.TradingAlgorithm
import domain.assets.security.SecurityHolding
import domain.assets.security.SecurityIdentifier
import application.handlers.TradingOrderHandler
import domain.signal.TradingDispatcher
import domain.trader.Trader
import domain.trader.TradingEngine
import infrastructure.network.IMarketDataProvider
import service.trader.fake.FakeTradingService

class TraderTester {
    suspend fun runTest() {
        val appleIdentifier = SecurityIdentifier(
            "US0378331005",
            "USD",
            "Apple"
        )

        val appleTrader = Trader(
            appleIdentifier,
            createHoldings(),
            5_000.0,
            TradingAlgorithm.Type.TACPP46,
        )

        val metaIdentifier = SecurityIdentifier(
            "US30303M1027",
            "USD",
            "Meta"
        )

        val metaTrader = Trader(
            metaIdentifier,
            createHoldings(),
            10_000.0,
            TradingAlgorithm.Type.TACPP46,
        )

        val traders = listOf(appleTrader, metaTrader)


        val tradingService = FakeTradingService()
        val signalDispatcher = TradingDispatcher()
        signalDispatcher.register(TradingOrderHandler(tradingService))
        val tradingEngine = TradingEngine(signalDispatcher)


        val marketDataProvider = IMarketDataProvider.create(IMarketDataProvider.Type.Finnhub)
        traders.forEach { trader ->
            val quote = marketDataProvider.getQuote(trader.securityIdentifier)
            val capitalBeforeSignal = trader.getCurrentCapital()
            val holdingsBeforeSignal = trader.getHoldings().toList()
            val signal = tradingEngine.onQuote(trader, quote)

            println("#================================================#")
            println("Trader: ${trader.securityIdentifier.name}")
            println("ISIN: ${trader.securityIdentifier.isin}")
            println("Currency: ${trader.securityIdentifier.currency}")
            println("Current price: ${quote.currentPrice}")
            println()

            println("Capital before signal: $capitalBeforeSignal")
            println("Capital after signal: ${trader.getCurrentCapital()}")
            println()

            println("Signal: ${signal.toReadableText()}")
            println()

            println("Holdings before signal:")
            printHoldings(holdingsBeforeSignal)

            println("Holdings after signal:")
            printHoldings(trader.getHoldings())

            println("#================================================#")
            println()
        }
    }

    private fun createHoldings(): MutableList<SecurityHolding>{
        return mutableListOf(
            SecurityHolding( 250.0,  2),
            SecurityHolding( 270.0,  3),
            SecurityHolding( 290.0,  1),
            SecurityHolding( 310.0,  2),
            SecurityHolding( 860.0,  3)
        )
    }

    private fun printHoldings(holdings: List<SecurityHolding>) {
        if (holdings.isEmpty()) {
            println("        None")
            return
        }

        holdings.forEach { holding ->
            println("        $holding")
        }
    }
}