package domain

import application.tester.TraderTester
import application.tester.TradingAlgorithmBackTester
import domain.algorithm.TradingAlgorithm
import domain.assets.security.SecurityIdentifier
import domain.tax.Taxation
import kotlin.time.Instant

suspend fun main() {
    val startDate = Instant.parse("2020-01-01T00:00:00Z") // 2020.01.01 0:00:00
    val endDate = Instant.parse("2025-01-01T00:00:00Z") // 2024.01.01 0:00:00

    val identifier = SecurityIdentifier(
        "US67066G1040",
        "USD",
        "NVIDIA"
    )

    TradingAlgorithmBackTester(
        type = TradingAlgorithm.Type.TACPP46,
        securityIdentifier = identifier,
        startingCapital = 10000.0,
        from = startDate,
        to = endDate
    ).runBackTest(TradingAlgorithmBackTester.DisplayMode.Display())

    TradingAlgorithmBackTester(
        type = TradingAlgorithm.Type.TACPP46,
        securityIdentifier = identifier,
        startingCapital = 10000.0,
        taxation = Taxation.get(Taxation.Type.Hungary),
        from = startDate,
        to = endDate
    ).runBackTest(TradingAlgorithmBackTester.DisplayMode.Display(TradingAlgorithmBackTester.DebugMode.Holding))

    val t = TraderTester()
    t.runTest()
}