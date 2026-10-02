package infrastructure.broker

import api.dto.CreateTraderRequest
import api.dto.SecurityIdentifierRequest
import application.logging.logger
import api.service.portfolio.IPortfolioService
import api.service.trader.ITraderService
import application.service.broker.InteractiveBrokersOrderService
import application.tester.TradingAlgorithmEvaluator
import data.network.ibkr.backtest.BacktestDataService
import data.repository.historical_data.IHistoricalMarketDataProvider
import data.repository.user.IUserRepository
import data.repository.user.sql.toDomain
import domain.algorithm.TradingAlgorithm
import domain.market.security.SecurityIdentifier
import domain.tax.Taxation
import domain.trader.TradingOrder
import kotlinx.coroutines.*
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.util.UUID
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days
import kotlin.time.Instant

@Deprecated("ONLY TESTING")
@Component
class Test(
    private val orderService: InteractiveBrokersOrderService,
    private val traderService: ITraderService,
    private val portfolioService: IPortfolioService,
    private val backtestDataService: BacktestDataService,
    @param:Qualifier("yahoo")
    private val provider: IHistoricalMarketDataProvider,
    private val userRepository: IUserRepository,
) {
    private val logger = logger<Test>()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val startCapital = 10_000.0
    private val startDate = Instant.parse("2021-01-01T00:00:00Z")
    private val endDate = Instant.parse("2026-01-01T00:00:00Z")
    private val evaluationWindowStepYears = 1 // default: 1 - for accurate results.
    private val userId = UUID.fromString("f0792158-24a0-427f-999d-6cf8fa3a0cf3")

    @Scheduled(
        cron = "0 32 20 * * *",
        zone = "Europe/Budapest"
    )
    fun placeConcurrentTestOrders() {
        scope.launch {
            try {
                val user = userRepository.getById(userId).getOrThrow().toDomain()
                val portfolio = user.portfolios.elementAt(0)
                val orders = coroutineScope {
                    portfolio.traders.map { trader ->
                        async {
                            try {
                                val order = traderService.executeTrader(trader.id)

                                logger.info(
                                    "Submitting trader={} order={}",
                                    trader.securityIdentifier.tickerSymbol,
                                    order.toString()
                                )

                                order
                            } catch (e: Exception) {
                                logger.error(
                                    "Failed trader={}",
                                    trader.id,
                                    e
                                )
                                null
                            }
                        }
                    }
                }.awaitAll()

                orders
                    .filterNotNull()
                    .forEach { order ->
                        orderService.submit(order)
                    }

                logger.info("All concurrent trader jobs finished")

            } catch (e: Exception) {
                logger.error(
                    "Concurrent trading test failed",
                    e
                )
            }
        }
    }
    @Scheduled(
        cron = "0 51  0 * * *",
        zone = "Europe/Budapest"
    )
    fun getHistoricalData(){
        scope.launch {
            val identifier = SecurityIdentifier(
                isin = "US02079K3059",
                tickerSymbol = "GOOGL",
                currency = "USD",
            )

            val to = Clock.System.now()
            val from = to - (365.days)

            backtestDataService.download(identifier, from, to)
        }
    }

    @Scheduled(
        cron = "0 22 23 * * *",
        zone = "Europe/Budapest"
    )
    fun sellAllHolding() {
        scope.launch {
            val user = userRepository.getById(userId).getOrThrow().toDomain()
            val portfolio = user.portfolios.elementAt(0)

            portfolio.traders.forEach { trader ->

                if(trader.holdings.isNotEmpty()){
                    val orders = traderService.forceSellAllHolding(
                        trader.id
                    )

                    orders.forEach { order ->
                        orderService.submit(order)
                    }
                }
            }
        }
    }

    @Scheduled(
        cron = "0 06 23 * * *",
        zone = "Europe/Budapest"
    )
    fun buyHolding(){
        scope.launch {
            try {
                val user = userRepository.getById(userId).getOrThrow().toDomain()
                val portfolio = user.portfolios.elementAt(0)

                portfolio.traders.forEach { trader ->
                        val order = TradingOrder(
                            traderId = trader.id,
                            signal = TradingAlgorithm.Output(TradingAlgorithm.Output.Buy(amount = 3), null),
                            atPrice = 433.0,
                            securityIdentifier = trader.securityIdentifier,
                        )
                        orderService.submit(order)
                }
            } catch(e: Exception){
                throw e
            }
        }
    }
    @Scheduled(
        cron = "0 03 23 * * *",
        zone = "Europe/Budapest"
    )
    fun createTraders() {
        scope.launch {
            var securityList = provider.getAllSecurityIdentifiers().getOrThrow()

            val user = userRepository.getById(userId).getOrThrow().toDomain()
            val traders = user.portfolios.elementAt(0).traders
            val traderSecurities = traders.map {
                trader-> trader.securityIdentifier
            }
            securityList = securityList.filter { security ->
                security !in traderSecurities
            }

            securityList.forEach { security ->
                traderService.createTrader(
                    userId = userId,
                    request = CreateTraderRequest(
                        securityIdentifier = SecurityIdentifierRequest(
                            isin = security.isin,
                            tickerSymbol = security.tickerSymbol,
                            currency = security.currency,
                        ),
                        capital = 20000.0,
                        algorithmType = "TACPP46"
                    )
                )
            }
        }
    }

    @Scheduled(
        cron = "0 43 18 * * *",
        zone = "Europe/Budapest"
    )
    fun deleteTraders() {
        scope.launch {
            sellAllHolding()
            val portfolio = portfolioService.getPortfolioByUserId(userId)
            portfolio.traders.forEach { trader ->
                traderService.deleteTrader(userId, trader.id)
            }
        }
    }

    @Scheduled(
        cron = "0 29 20 * * *",
        zone = "Europe/Budapest"
    )
    fun createTradersByEvalOutput() {
        scope.launch {
            var evalOutput = TradingAlgorithmEvaluator(
                provider = provider,
                tradingAlgorithmType = TradingAlgorithm.Type.TACPP46,
                capital = startCapital,
                taxation = Taxation.Type.Hungary,
                evaluationStartYear = startDate,
                evaluationEndYear = endDate,
                windowStepYears = evaluationWindowStepYears
            ).runEvaluationOnAll().getBestList(10)

            val traders = traderService.getAll(userId)
            val traderSecurities = traders.map {
                    trader-> trader.securityIdentifier
            }
            evalOutput = evalOutput.filter { security ->
                security !in traderSecurities
            }

            evalOutput.forEach { security ->
                traderService.createTrader(
                    userId = userId,
                    request = CreateTraderRequest(
                        securityIdentifier = SecurityIdentifierRequest(
                            isin = security.isin,
                            tickerSymbol = security.tickerSymbol,
                            currency = security.currency,
                        ),
                        capital = 20_000.0,
                        algorithmType = "TACPP46"
                    )
                )
            }
        }
    }
}