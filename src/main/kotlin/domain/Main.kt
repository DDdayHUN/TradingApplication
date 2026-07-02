package domain

import application.backtest.TradingAlgorithmBackTester
import domain.algorithm.TradingAlgorithm
import domain.assets.security.SecurityIdentifier
import domain.tax.ITaxation
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

    /*
    val FN = FinnhubTester()
    FN.runFinnhubTester(
        SecurityIdentifier(
            "US0378331005",
            "USD",
            "Apple"
        )
    )
    */
}