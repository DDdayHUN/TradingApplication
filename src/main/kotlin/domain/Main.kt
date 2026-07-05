package domain

import application.tester.TraderTester
import application.tester.TradingAlgorithmBackTester
import application.tester.TradingAlgorithmEvaluater
import data.repository.trader.FakeTraderRepository
import domain.algorithm.TradingAlgorithm
import domain.assets.security.SecurityHolding
import domain.assets.security.SecurityIdentifier
import domain.tax.Taxation
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

    val c_RUN_BACKTEST_ON_ONE = true
    val c_RUN_EVAL_ON_ONE = true
    val c_RUN_EVAL_ON_ALL = false    // NOTE : This might take some time, it is a VERY HEAVY COMPUTATION :)
    val c_RUN_TRADER_TEST = false

    val c_ALGORITHM = TradingAlgorithm.Type.TACPP46

    //===========================================================//
    //===========================================================//
    // Config

    val taxation = Taxation.Type.Hungary

    val identifier = SecurityIdentifier(
        "US64110L1061",
        "USD",
        "NETFLIX"
    )

    val startCapital = 10000.0
    val startDate = Instant.parse("2020-01-01T00:00:00Z")
    val endDate = Instant.parse("2025-01-01T00:00:00Z")

    //===========================================================//
    //===========================================================//

    if(c_RUN_BACKTEST_ON_ONE){
        run{
            TradingAlgorithmBackTester(
                type = c_ALGORITHM,
                securityIdentifier = identifier,
                startingCapital = startCapital,
                taxation = Taxation.create(taxation),
                from = startDate,
                to = endDate
            ).runBackTest(TradingAlgorithmBackTester.DisplayMode.Display())
        }
    }

    if(c_RUN_EVAL_ON_ONE){
        run{
            TradingAlgorithmEvaluater(c_ALGORITHM, startCapital, taxation)
                .runEvaluation()
        }
    }

    if(c_RUN_EVAL_ON_ALL){
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

    if(c_RUN_TRADER_TEST){
        run {
            val repo = FakeTraderRepository
            val traderList = repo.getAll()

            if(traderList.firstOrNull()?.securityIdentifier?.isin != identifier.isin){
                clearTestFolder()
            }

            TradingAlgorithm.Type.entries.forEachIndexed { index, type ->
                val uuid = traderList.getOrNull(index)?.uuid ?: UUID.randomUUID()

                TraderTester(
                    securityIdentifier = identifier,
                    holdings = mutableListOf<SecurityHolding>(),
                    capital = 2_000.0,
                    algorithmType = type
                ).runTest(uuid)
            }
        }
    }
}