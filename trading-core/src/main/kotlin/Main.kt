import application.provider.HistoricalMarketDataProvider
import application.tester.TradingAlgorithmBackTester
import application.tester.TradingAlgorithmEvaluator
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
    // Backtest

    val c_RUN_BACKTEST_ON_ONE_SECURITY = false
    val c_RUN_BACKTEST_ON_N_SECURITY = false
    val c_RUN_BACKTEST_ON_ALL_SECURITY = false

    //===========================================================//
    //===========================================================//
    // Eval

    val c_RUN_EVAL_ON_ONE_ALGORITHM = false
    val c_RUN_EVAL_ON_ONE_ALGORITHM_WITH_BEST_OUTPUT = true; val percentToGetAfterEval = 0.07
    val c_RUN_EVAL_ON_ONE_ALGORITHM_N_SECURITY = false
    val c_RUN_EVAL_ON_ALL_ALGORITHM = false

    //===========================================================//
    //===========================================================//
    // Config

    val algorithm = TradingAlgorithm.Type.TACPP46
    val taxation = Taxation.Type.Hungary

    val identifier = SecurityIdentifier(
        "US0378331005",
        "AAPL",
        "USD"
    )

    val identifierList = listOf(
        identifier,
        SecurityIdentifier(
            "US30303M1027",
            "META",
            "USD"
        ),
        SecurityIdentifier(
            "US5949181045",
            "MSFT",
            "USD"
        ),
        SecurityIdentifier(
            "US67066G1040",
            "NVDA",
            "USD"
        ),
        SecurityIdentifier(
            "US0079031078",
            "AMD",
            "USD"
        ),
        SecurityIdentifier(
            "US0231351067",
            "AMZN",
            "USD"
        ),
        SecurityIdentifier(
            "US6541061031",
            "NKE",
            "USD"
        )
    )
    val startCapital = 10_000.0
    val startDate = Instant.parse("2021-01-01T00:00:00Z")
    val endDate = Instant.parse("2026-01-01T00:00:00Z")
    val evaluationWindowStepYears = 1 // default: 1 - for accurate results.

    val yahooHistoricalMarketDataProvider =
        HistoricalMarketDataProvider.get(HistoricalMarketDataProvider.Type.YahooHistoricalMarketDataRepository)

    val ibkrHistoricalMarketDataProvider =
        HistoricalMarketDataProvider.get(HistoricalMarketDataProvider.Type.IbkrHistoricalMarketDataRepository)

    //===========================================================//
    //===========================================================//
    // Config Checks

    // Backtest
    var c_BACKTEST_CONFIG_ERROR = false
    if(c_RUN_BACKTEST_ON_ONE_SECURITY && c_RUN_BACKTEST_ON_N_SECURITY) c_BACKTEST_CONFIG_ERROR = true
    if(c_RUN_BACKTEST_ON_ONE_SECURITY && c_RUN_BACKTEST_ON_ALL_SECURITY) c_BACKTEST_CONFIG_ERROR = true
    if(c_RUN_BACKTEST_ON_ALL_SECURITY && c_RUN_BACKTEST_ON_N_SECURITY) c_BACKTEST_CONFIG_ERROR = true

    // Eval
    var c_EVAL_CONFIG_ERROR = false
    // TODO : This shet

    //===========================================================//
    // Config Errors

    if(c_BACKTEST_CONFIG_ERROR && c_EVAL_CONFIG_ERROR) error("Cannot run Backtest and Eval at the same time")
    if(c_BACKTEST_CONFIG_ERROR) error("Backtest incorrectly set")
    if(c_EVAL_CONFIG_ERROR) error("Eval incorrectly set")

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
            ).runEvaluationOnAll().display()
        }
    }

    //===========================================================//

    if(c_RUN_EVAL_ON_ONE_ALGORITHM_WITH_BEST_OUTPUT){
        run {
            val first = TradingAlgorithmEvaluator(
                yahooHistoricalMarketDataProvider,
                algorithm,
                startCapital,
                taxation,
                startDate,
                endDate,
                evaluationWindowStepYears
            ).runEvaluationOnAll()
            first.display()

            val size = (first.getBestList().size * percentToGetAfterEval).toInt()
            println("list size: ${first.getBestList().size}")
            println("size after: $size")
            TradingAlgorithmEvaluator(
                yahooHistoricalMarketDataProvider,
                algorithm,
                startCapital,
                taxation,
                startDate,
                endDate,
                evaluationWindowStepYears
            ).runEvaluation(first.getBestList(size)).display()
        }
    }

    //===========================================================//

    if(c_RUN_EVAL_ON_ONE_ALGORITHM_N_SECURITY) {
        run {
            TradingAlgorithmEvaluator(
                yahooHistoricalMarketDataProvider,
                algorithm,
                startCapital,
                taxation,
                startDate,
                endDate,
                evaluationWindowStepYears
            ).runEvaluation(identifierList).display()
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
                        ).runEvaluationOnAll()
                    }
                }.awaitAll()

                listOfOutput.forEach {
                    it.display()
                }
            }
        }
    }
}