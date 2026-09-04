import application.tester.TradingAlgorithmBackTester
import application.tester.TradingAlgorithmEvaluator
import application.provider.HistoricalMarketDataProvider
import domain.algorithm.TradingAlgorithm
import domain.market.security.SecurityIdentifier
import domain.tax.Taxation
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlin.time.Instant

suspend fun main() {
    //===========================================================//
    //===========================================================//
    // Settings

    val c_RUN_BACKTEST_ON_ONE_SECURITY = false
    val c_RUN_BACKTEST_ON_ALL_SECURITY = false // NOTE : This might take some time, it is a HEAVY COMPUTATION :)
    val c_RUN_EVAL_ON_ONE_ALGORITHM = false
    val c_RUN_EVAL_ON_ALL_ALGORITHM = false // NOTE : This might take some time, it is a VERY HEAVY COMPUTATION :)

    //===========================================================//
    //===========================================================//
    // Config

    val algorithm = TradingAlgorithm.Type.TACPP46
    val taxation = Taxation.Type.Hungary

    val identifier = SecurityIdentifier(
        "US0079031078",
        "AMD",
        "USD"
    )

    val startCapital = 5000.0
    val startDate = Instant.parse("2020-01-01T00:00:00Z")
    val endDate = Instant.parse("2026-01-01T00:00:00Z")
    val evaluationWindowStepYears = 1 // default: 1 - for accurate results.

    val yahooHistoricalMarketDataProvider =
        HistoricalMarketDataProvider.get(HistoricalMarketDataProvider.Type.YahooHistoricalMarketDataRepository)

    //===========================================================//
    //===========================================================//
    // Config Checks

    if(c_RUN_EVAL_ON_ONE_ALGORITHM && c_RUN_EVAL_ON_ALL_ALGORITHM) error("You can't run eval on one algorithm and on all at the same time")
    if(c_RUN_BACKTEST_ON_ONE_SECURITY && c_RUN_BACKTEST_ON_ALL_SECURITY) error("You can't run backtest on one security and on all at the same time")
    if(c_RUN_BACKTEST_ON_ALL_SECURITY && c_RUN_EVAL_ON_ONE_ALGORITHM) error("You can't run backtest on all security and eval on the same algorithm at the same time")

    //===========================================================//
    //===========================================================//
    // Tests

    if(c_RUN_BACKTEST_ON_ONE_SECURITY) {
        run{
            TradingAlgorithmBackTester(
                provider = yahooHistoricalMarketDataProvider,
                type = algorithm,
                securityIdentifier = identifier,
                startingCapital = startCapital,
                taxation = taxation,
                from = startDate,
                to = endDate
            ).runBackTest().display()
        }
    }

    //===========================================================//

    if(c_RUN_BACKTEST_ON_ALL_SECURITY) {
        run {
            coroutineScope {
                val listOfOutput = yahooHistoricalMarketDataProvider
                    .getAllSecurityIdentifiers()
                    .getOrThrow().map {
                        async {
                            TradingAlgorithmBackTester(
                                provider = yahooHistoricalMarketDataProvider,
                                type = algorithm,
                                securityIdentifier = it,
                                startingCapital = startCapital,
                                taxation = taxation,
                                from = startDate,
                                to = endDate
                            ).runBackTest()
                        }
                }.awaitAll()

                listOfOutput.forEach {
                    it.display()
                }
            }
        }
    }

    //===========================================================//

    if(c_RUN_EVAL_ON_ONE_ALGORITHM) {
        run{
            TradingAlgorithmEvaluator(
                yahooHistoricalMarketDataProvider,
                algorithm,
                startCapital,
                taxation,
                startDate,
                endDate,
                evaluationWindowStepYears
            ).runEvaluation().display()
        }
    }

    //===========================================================//

    if(c_RUN_EVAL_ON_ALL_ALGORITHM) {
        run {
            coroutineScope {
                val listOfOutput = TradingAlgorithm.Type.entries.map {
                    async {
                        TradingAlgorithmEvaluator(
                            yahooHistoricalMarketDataProvider,
                            it,
                            startCapital,
                            taxation,
                            startDate,
                            endDate,
                            evaluationWindowStepYears
                        ).runEvaluation()
                    }
                }.awaitAll()

                listOfOutput.forEach {
                    it.display()
                }
            }
        }
    }
}