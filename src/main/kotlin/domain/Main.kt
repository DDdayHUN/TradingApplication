package domain

import application.tester.TraderTester
import application.tester.TradingAlgorithmBackTester
import application.tester.TradingAlgorithmEvaluater
import domain.algorithm.TradingAlgorithm
import domain.assets.security.SecurityIdentifier
import domain.tax.Taxation
import kotlin.time.Instant

suspend fun main() {
    run {
        val algType = TradingAlgorithm.Type.TACPP46
        val identifier = SecurityIdentifier(
            "US67066G1040",
            "USD",
            "NVIDIA"
        )
        val startCapital = 10000.0
        val startDate = Instant.parse("2020-01-01T00:00:00Z")
        val endDate = Instant.parse("2025-01-01T00:00:00Z")

        TradingAlgorithmBackTester(
            type = algType,
            securityIdentifier = identifier,
            startingCapital = startCapital,
            taxation = Taxation.create(Taxation.Type.Hungary),
            from = startDate,
            to = endDate
        ).runBackTest(TradingAlgorithmBackTester.DisplayMode.Display())

        TradingAlgorithmEvaluater(algType, Taxation.create(Taxation.Type.Hungary), startCapital)
            .runEvaluation()
    }

    run {
        TraderTester()
            .runTest()
    }
}