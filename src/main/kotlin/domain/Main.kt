package domain

import application.backtest.TradingAlgorithmBackTester
import domain.algorithm.TradingAlgorithm
import domain.assets.security.SecurityHolding
import domain.assets.security.SecurityIdentifier
import domain.signal.TradingSignal
import domain.tax.ITaxation
import domain.trader.Trader
import infrastructure.network.IMarketDataProvider
import java.util.UUID
import kotlin.time.Instant

suspend fun main() {
    val startDate = Instant.parse("2020-01-01T00:00:00Z") // 2020.01.01 0:00:00
    val endDate = Instant.parse("2025-01-01T00:00:00Z") // 2024.01.01 0:00:00

    val identifier = SecurityIdentifier(
        "US0378331005",
        "USD",
        "Apple"
    )

    val bt = TradingAlgorithmBackTester(
        ITaxation.HUNGARY,
        TradingAlgorithm.Type.TACPP46,
        identifier,
        10000.0,
        startDate,
        endDate)
    bt.runBackTest()

//    val holdings = mutableListOf(
//        SecurityHolding( 250.0,  2),
//        SecurityHolding( 270.0,  3),
//        SecurityHolding( 290.0,  1),
//        SecurityHolding( 310.0,  2),
//        SecurityHolding( 860.0,  3)
//    )
//
//
//    val marketDataProvider = IMarketDataProvider.create(
//        IMarketDataProvider.Type.Finnhub
//    )
//
//    val appleIdentifier = SecurityIdentifier(
//        "US0378331005",
//        "USD",
//        "Apple"
//    )
//
//    val appleAlg = TradingAlgorithm.create(
//        TradingAlgorithm.Type.TACPP46,
//        appleIdentifier,
//    ).second
//
//    val appleTrader = Trader(
//        UUID.randomUUID(),
//        appleIdentifier.name,
//        appleIdentifier,
//        5_000.0,
//        holdings,
//        appleAlg
//    )
//
//    val metaIdentifier = SecurityIdentifier(
//        "US30303M1027",
//        "USD",
//        "Meta"
//    )
//
//    val metaAlg = TradingAlgorithm.create(
//        TradingAlgorithm.Type.TACPP46,
//        metaIdentifier,
//    ).second
//
//
//    val metaTrader = Trader(
//        UUID.randomUUID(),
//        metaIdentifier.name,
//        metaIdentifier,
//        10_000.0,
//        holdings,
//        metaAlg
//    )
//
//    val traders = listOf(appleTrader, metaTrader)
//
//    traders.forEach { trader ->
//        val quote = marketDataProvider.getQuote(trader.securityIdentifier)
//
//        val signals = trader.createSignals(quote)
//
//        println("#================================================#")
//        println("Trader: ${trader.stockName}")
//        println("ISIN: ${trader.securityIdentifier.isin}")
//        println("Currency: ${trader.securityIdentifier.currency}")
//        println("Current price: ${quote.currentPrice}")
//        println("Allocated capital: ${trader.getAllocatedCapital()}")
//        println("Holdings: ${trader.getHoldings()}")
//
//
//        println("Signals:")
//        signals.forEach { signal ->
//            println(signal)
//        }
//    }
}