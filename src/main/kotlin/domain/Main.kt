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
        val taxation = Taxation.get(Taxation.Type.Hungary)
        val startDate = Instant.parse("2020-01-01T00:00:00Z")
        val endDate = Instant.parse("2025-01-01T00:00:00Z")

        TradingAlgorithmBackTester(
            type = algType,
            securityIdentifier = identifier,
            startingCapital = startCapital,
            from = startDate,
            to = endDate
        ).runBackTest(TradingAlgorithmBackTester.DisplayMode.Display())

        TradingAlgorithmBackTester(
            type = algType,
            securityIdentifier = identifier,
            startingCapital = startCapital,
            taxation = taxation,
            from = startDate,
            to = endDate
        ).runBackTest(TradingAlgorithmBackTester.DisplayMode.Display(TradingAlgorithmBackTester.DebugMode.Holding))

        TradingAlgorithmEvaluater(algType, taxation, startCapital, startDate, endDate)
            .runEvaluation()
    }

    run {
        TraderTester()
            .runTest()
    }
}