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

    val bt = TradingAlgorithmBackTester(
        Taxation.get(Taxation.Type.Hungary),
        TradingAlgorithm.Type.TACPP46,
        identifier,
        10000.0,
        startDate,
        endDate
    )
    bt.runBackTest()

    val t = TraderTester()
    t.runTest()
}