package domain

import application.tester.TraderTester
import application.tester.TradingAlgorithmBackTester
import application.tester.TradingAlgorithmEvaluater
import data.repository.trader.FakeTraderRepository
import domain.algorithm.TradingAlgorithm
import domain.assets.security.SecurityHolding
import domain.assets.security.SecurityIdentifier
import domain.tax.Taxation
import java.util.UUID
import kotlin.time.Instant

suspend fun main() {
    val s_RUN_TRADER_TEST = false

    run {
        TradingAlgorithm.Type.entries.forEach { type ->
            val identifier = SecurityIdentifier(
                "US67066G1040",
                "USD",
                "NVIDIA"
            )
            val startCapital = 10000.0
            val startDate = Instant.parse("2020-01-01T00:00:00Z")
            val endDate = Instant.parse("2025-01-01T00:00:00Z")

            TradingAlgorithmBackTester(
                type = type,
                securityIdentifier = identifier,
                startingCapital = startCapital,
                taxation = Taxation.create(Taxation.Type.Hungary),
                from = startDate,
                to = endDate
            ).runBackTest(TradingAlgorithmBackTester.DisplayMode.Display())
            println("Algorithm: $type")
            TradingAlgorithmEvaluater(type, startCapital, Taxation.Type.Hungary)
                .runEvaluation()
        }
    }

    if(s_RUN_TRADER_TEST){
        run {
            val repo = FakeTraderRepository
            val traderList = repo.getAll()

            val securityIdentifier = SecurityIdentifier(
                "US67066G1040",
                "USD",
                "NVIDIA"
            )

            TradingAlgorithm.Type.entries.forEachIndexed { index, type ->
                val uuid = traderList.getOrNull(index)?.uuid ?: UUID.randomUUID()

                TraderTester(
                    securityIdentifier = securityIdentifier,
                    holdings = mutableListOf<SecurityHolding>(),
                    capital = 2_000.0,
                    algorithmType = type
                ).runTest(uuid)
            }
        }
    }
}