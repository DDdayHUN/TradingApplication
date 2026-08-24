import application.service.borker.InteractiveBrokersService
import application.service.broker.BrokerOrderRequest
import application.service.broker.BrokerOrderSide
import application.service.broker.IBrokerService
import application.tester.TraderTester
import application.tester.TradingAlgorithmBackTester
import application.tester.TradingAlgorithmEvaluator
import data.network.MarketDataProvider
import data.network.ibkr.IbkrMarketDataProvider
import data.repository.historical_data.HistoricalMarketDataProvider
import data.repository.trader.TraderRepositoryProvider
import domain.algorithm.TradingAlgorithm
import domain.interfaces.IMarketDataProvider
import domain.market.security.SecurityIdentifier
import domain.tax.Taxation
import domain.trader.Trader
import infrastructure.broker.IbkrClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlin.time.Instant

suspend fun main() {
    //===========================================================//
    //===========================================================//
    // Settings

    val c_RUN_BACKTEST_ON_ONE_SECURITY = false
    val c_RUN_BACKTEST_ON_ALL_SECURITY = false // NOTE : This might take some time, it is a HEAVY COMPUTATION :)
    val c_RUN_EVAL_ON_ONE_ALGORITHM = false
    val c_RUN_EVAL_ON_ALL_ALGORITHM = false // NOTE : This might take some time, it is a VERY HEAVY COMPUTATION :)

    val c_RUN_TRADER_TEST = false
    val c_CLEAR_TRADER_TEST_FOLDER = false
    val c_RUN_IBKR_TEST = false

    //===========================================================//
    //===========================================================//
    // Config

    val algorithm = TradingAlgorithm.Type.TACPP46
    val taxation = Taxation.Type.Hungary

    val identifier = SecurityIdentifier(
        "US67066G1040",
        "NVDA",
        "USD"
    )

    val startCapital = 1000.0
    val startDate = Instant.parse("2025-06-01T00:00:00Z")
    val endDate = Instant.parse("2026-01-01T00:00:00Z")
    val evaluationWindowStepYears = 1 // default: 1 - for accurate results.

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
                val listOfOutput = HistoricalMarketDataProvider.getAllSecurityIdentifiers().getOrThrow().map {
                    async {
                        TradingAlgorithmBackTester(
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

    //===========================================================//

    if(c_RUN_TRADER_TEST) {
        run {

            if (c_CLEAR_TRADER_TEST_FOLDER) clearTestFolder()

            val traderList = TraderRepositoryProvider.get(TraderRepositoryProvider.Type.Fake).getAll().getOrThrow()

            val tradersToTest =
                if (traderList.any { it.securityIdentifier.isin == identifier.isin }) traderList
                else {
                    traderList + Trader(
                        securityIdentifier = identifier,
                        holdings = mutableSetOf(),
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

    //===========================================================//

    if (c_RUN_IBKR_TEST) {
        withContext(Dispatchers.Default) {
            val client = IbkrClient()
            val brokerService: IBrokerService =
                InteractiveBrokersService(client)

            val provider: IMarketDataProvider = IbkrMarketDataProvider(client)

            try {
                brokerService.connect()
                val price = provider.getQuote(identifier).getOrThrow().currentPrice
                println("price: {$price}")
                val accountSummary = brokerService.getAvailableCapital()
                println("summary: {$accountSummary}")

                /*brokerService.placeOrder(
                    request = BrokerOrderRequest(
                        ticker = identifier.tickerSymbol,
                        currency = identifier.currency,
                        quantity = 1.0,
                        side = BrokerOrderSide.BUY
                    )
                )*/

                brokerService.requestOrderStatus()

                delay(60_000)

            }
            finally {
                brokerService.disconnect()
            }
        }
    }
}