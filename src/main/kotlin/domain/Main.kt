package domain

import application.tester.TraderTester
import application.tester.TradingAlgorithmBackTester
import application.tester.TradingAlgorithmEvaluater
import data.repository.trader.FakeTraderRepository
import domain.algorithm.TradingAlgorithm
import domain.assets.security.SecurityIdentifier
import domain.tax.Taxation
import domain.trader.Trader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import utils.clearTestFolder
import java.util.UUID
import kotlin.time.Instant

suspend fun main() {
    //===========================================================//
    //===========================================================//
    // Settings

    val c_RUN_BACKTEST_ON_ONE = false
    val c_RUN_EVAL_ON_ONE = false
    val c_RUN_EVAL_ON_ALL = false    // NOTE : This might take some time, it is a VERY HEAVY COMPUTATION :)
    val c_RUN_TRADER_TEST = true
    val c_CLEAR_TRADER_TEST_FOLDER = true

    //===========================================================//
    //===========================================================//
    // Config

    val algorithm = TradingAlgorithm.Type.TACPP46
    val taxation = Taxation.Type.Hungary

    val identifier = SecurityIdentifier(
        "US0231351067",
        "USD",
        "AMAZON"
    )

    val startCapital = 500.0
    val startDate = Instant.parse("2020-01-01T00:00:00Z")
    val endDate = Instant.parse("2025-01-01T00:00:00Z")

    //===========================================================//
    //===========================================================//
    // Config Checks

    if(c_RUN_EVAL_ON_ONE && c_RUN_EVAL_ON_ALL) error("U can't run eval on one algorithm and on all at the same time")

    //===========================================================//
    //===========================================================//
    // Tests

    if(c_RUN_BACKTEST_ON_ONE) {
        run{
            TradingAlgorithmBackTester(
                type = algorithm,
                securityIdentifier = identifier,
                startingCapital = startCapital,
                taxation = Taxation.create(taxation),
                from = startDate,
                to = endDate
            ).runBackTest(TradingAlgorithmBackTester.DisplayMode.Display())
        }
    }

    if(c_RUN_EVAL_ON_ONE) {
        run{
            TradingAlgorithmEvaluater(algorithm, startCapital, taxation)
                .runEvaluation()
        }
    }

    if(c_RUN_EVAL_ON_ALL) {
        run {
            coroutineScope {
                TradingAlgorithm.Type.entries
                    .map { type ->
                        launch(Dispatchers.Default) {
                            TradingAlgorithmEvaluater(
                                type,
                                startCapital,
                                taxation
                            ).runEvaluation()
                        }
                    }.joinAll()
            }
        }
    }

    if(c_RUN_TRADER_TEST) {
        run {

            if (c_CLEAR_TRADER_TEST_FOLDER) clearTestFolder()

            val traderList = FakeTraderRepository.getAll()

            val tradersToTest =
                if (traderList.any { it.securityIdentifier.isin == identifier.isin }) {
                    traderList
                } else {
                    traderList + Trader(
                        uuid = UUID.randomUUID(),
                        securityIdentifier = identifier,
                        holdings = mutableListOf(),
                        allocatedCapital = startCapital,
                        algorithm = TradingAlgorithm.create(
                            algorithm,
                            securityIdentifier = identifier,
                        )
                    )
                }

            tradersToTest.forEach { trader ->
                TraderTester(trader).runTest()
            }
        }
    }
}