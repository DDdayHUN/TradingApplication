package application.tester

import domain.algorithm.TradingAlgorithm
import domain.assets.security.SecurityHolding
import domain.assets.security.SecurityIdentifier
import application.handlers.TradingOrderHandler
import data.repository.trader.FakeTraderRepository
import domain.signal.TradingDispatcher
import domain.trader.Trader
import domain.trader.TradingEngine
import infrastructure.network.IMarketDataProvider
import service.trader.fake.FakeTradingService

class TraderTester {
    suspend fun runTest() {
        val traderRepository = FakeTraderRepository()

        val nvdaConfig = TraderTestConfig(
            SecurityIdentifier(
                "US67066G1040",
                "USD",
                "NVIDIA"
            ),
            2_000.0,
            TradingAlgorithm.Type.TACPP46
        )


        val traderEntries = listOf(
            createTraderEntry(nvdaConfig, traderRepository)
        )

        val tradingService = FakeTradingService()

        val signalDispatcher = TradingDispatcher()
        signalDispatcher.register(TradingOrderHandler(tradingService))

        val tradingEngine = TradingEngine(signalDispatcher)

        val marketDataProvider = IMarketDataProvider.create(
            IMarketDataProvider.Type.Finnhub
        )

        traderEntries.forEach { entry ->
            val trader = entry.trader

            val quote = marketDataProvider.getQuote(trader.securityIdentifier)

            val capitalBeforeSignal = trader.getCurrentCapital()
            val holdingsBeforeSignal = trader.getHoldings().toList()

            val signal = tradingEngine.onQuote(trader, quote)

            traderRepository.save(trader, entry.algorithmType)

            println("#================================================#")
            println("# Trader Testing")
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

    private suspend fun createTraderEntry(config: TraderTestConfig, traderRepository: FakeTraderRepository): TraderTestConfig.TraderEntry {
        val savedTrader = traderRepository.getBySecurityIdentifier(config.securityIdentifier)

        val trader = savedTrader ?: createTestTrader(config)

        return TraderTestConfig.TraderEntry(
            trader,
            config.algorithmType
        )
    }

    private fun printHoldings(holdings: List<SecurityHolding>) {
        if (holdings.isEmpty()) {
            println("None")
            return
        }

        holdings.forEach { holding ->
            println(holding.toString())
        }
    }

    private fun createTestTrader(config: TraderTestConfig): Trader {
        return Trader(
            config.securityIdentifier,
            mutableListOf<SecurityHolding>(SecurityHolding(100.0,2L)),
            config.startCapital,
            config.algorithmType
        )
    }

    private data class TraderTestConfig(
        val securityIdentifier: SecurityIdentifier,
        val startCapital: Double,
        val algorithmType: TradingAlgorithm.Type
    ){
        data class TraderEntry(
            val trader: Trader,
            val algorithmType: TradingAlgorithm.Type
        )
    }
}