package domain

import application.tester.TraderTester
import application.tester.TradingAlgorithmBackTester
import application.tester.TradingAlgorithmEvaluater
import domain.algorithm.TradingAlgorithm
import domain.assets.security.SecurityHolding
import domain.assets.security.SecurityIdentifier
import domain.tax.Taxation
import kotlin.time.Instant

suspend fun main() {
    run {
        val algType = TradingAlgorithm.Type.ALGDES3
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

        TradingAlgorithmEvaluater(algType, startCapital, Taxation.Type.Hungary)
            .runEvaluation()
    }

    /*
    run {
        TraderTester(
            SecurityIdentifier(
                "US67066G1040",
                "USD",
                "NVIDIA"
            ),
            mutableListOf(
                SecurityHolding(100.0,2L)
            ),
            2_000.0,
            TradingAlgorithm.Type.TACPP46
        )
            .runTest()
    }
    */
}