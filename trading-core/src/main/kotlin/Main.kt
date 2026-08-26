import application.service.borker.InteractiveBrokersService
import application.service.broker.IBrokerService
import application.service.broker.toBrokerOrder
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
import infrastructure.broker.IbkrConfig
import infrastructure.broker.IbkrSession
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

    val algorithm = TradingAlgorithm.Type.TACPP462
    val taxation = Taxation.Type.Hungary

    val identifier = SecurityIdentifier(
        "US0079031078",
        "AMD",
        "USD"
    )

    val startCapital = 1000.0
    val startDate = Instant.parse("2020-01-01T00:00:00Z")
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
            val config = IbkrConfig.fromEnv()
            val session = IbkrSession(client, config)

            val brokerService: IBrokerService = InteractiveBrokersService(session)
            val provider = MarketDataProvider.create(MarketDataProvider.Type.Ibkr(session))

            try {
                val trader = Trader(
                    securityIdentifier = identifier,
                    allocatedCapital = 10_000.0,
                    algorithm = TradingAlgorithm.create(
                        type = TradingAlgorithm.Type.TACPP46,
                        securityIdentifier = identifier,
                    )
                )
                val quote = provider.getQuote(trader.securityIdentifier).getOrThrow()
                println("Quote: ${quote.currentPrice}")
                val tradingOrder = trader.createOrder(quote)
                println("Trading order: ${tradingOrder.toReadableText()}")
                val brokerOrder = tradingOrder.toBrokerOrder()
                if(brokerOrder != null ){
                    val ibkrOrderId = brokerService.placeOrder(brokerOrder)
                    println("Submitted IBKR orderId=$ibkrOrderId")
                } else {
                    println("HOLD - no broker order created")
                }
            }
            finally {
                delay(60_000)
                session.disconnect()
            }
        }
    }
}