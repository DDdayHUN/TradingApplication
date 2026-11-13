package data.infrastructure.broker.ibkr

import api.dto.CreateTraderRequest
import api.dto.SecurityIdentifierRequest
import application.logging.logger
import application.provider.MarketDataProvider
import application.service.trader.ITraderService
import application.service.broker.ibkr.InteractiveBrokersService
import application.tester.TradingAlgorithmEvaluator
import data.network.IMarketDataProvider
import data.network.ibkr.backtest.BacktestDataService
import data.repository.historical_data.IHistoricalMarketDataProvider
import data.repository.user.IUserRepository
import data.repository.user.sql.toDomain
import domain.algorithm.TradingAlgorithm
import domain.market.security.SecurityIdentifier
import domain.tax.Taxation
import domain.trader.Trader
import domain.trader.TradingOrder
import kotlinx.coroutines.*
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.util.UUID
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days
import kotlin.time.Instant

//===========================================================//
/**
 * This is only for testing purposes.
 */
//===========================================================//

@Component
class Test(
    private val orderService: InteractiveBrokersService,
    private val traderService: ITraderService,
    private val backtestDataService: BacktestDataService,
    @param:Qualifier("yahoo")
    private val provider: IHistoricalMarketDataProvider,
    private val userRepository: IUserRepository,
    private val marketDataProvider: MarketDataProvider,
) {
    //===========================================================//
    //===========================================================//

    private val logger = logger<Test>()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val startCapital = 10_000.0
    private val startDate = Instant.parse("2021-01-01T00:00:00Z")
    private val endDate = Instant.parse("2026-01-01T00:00:00Z")
    private val evaluationWindowStepYears = 1 // default: 1 - for accurate results.
    private val userId = UUID.fromString("f0792158-24a0-427f-999d-6cf8fa3a0cf3")

    //===========================================================//
    //===========================================================//

    @Scheduled(
        cron = "0 33 17 * * *",
        zone = "Europe/Budapest"
    )
    fun getFinnhubMarketData(){
        scope.launch{
            try {
                val identifier = SecurityIdentifier(
                    isin = "US4370761029",
                    tickerSymbol = "HD",
                    currency = "USD"
                )
               val quote = marketDataProvider.get(MarketDataProvider.Type.Finnhub).getQuote(userId, identifier).getOrThrow()
                logger.info("Current Price for identifier: $quote")
            } catch(e: Exception){
                throw e
            }
        }
    }

    //===========================================================//

    fun placeConcurrentTestOrders() {
        scope.launch {
            try {
                val orders = traderService.executeAllTrader(userId)

                orders
                    .forEach { order ->
                        orderService.submitOrderForUser(userId, order)
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

    //===========================================================//

    fun getHistoricalData(){
        scope.launch {
            val identifier = SecurityIdentifier(
                isin = "US02079K3059",
                tickerSymbol = "GOOGL",
                currency = "USD",
            )

            val to = Clock.System.now()
            val from = to - (365.days)

            backtestDataService.download(userId,identifier, from, to)
        }
    }

    //===========================================================//

    fun sellAllHolding() {
        scope.launch {
            val orders = traderService.forceSellAllHolding(userId)
            orders.forEach { order ->
                orderService.submitOrderForUser(userId, order)
            }
        }
    }

    //===========================================================//

    fun buyHolding(){
        scope.launch {
            try {
                val user = userRepository.getById(userId).getOrThrow().toDomain()
                val portfolio = user.portfolio

                portfolio.traders.forEach { trader ->
                        val order = TradingOrder(
                            traderId = trader.id,
                            securityIdentifier = trader.securityIdentifier,
                            signal = TradingOrder.Signal.Buy(amount = 3),
                            atPrice = 433.0
                        )
                        orderService.submitOrderForUser(userId, order)
                }
            } catch(e: Exception){
                throw e
            }
        }
    }

    //===========================================================//

    fun createTraders() {
        scope.launch {
            val user = userRepository.getById(userId).getOrThrow().toDomain()
            val portfolio = user.portfolio
            val traders = portfolio.traders
            val traderSecurities = traders.map { trader-> trader.securityIdentifier }
            val securityList = provider.getAllSecurityIdentifiers().getOrThrow().filter { security -> security !in traderSecurities }

            securityList.forEach { security ->
                val trader = Trader(
                    securityIdentifier = security,
                    allocatedCapital = 20000.0,
                    algorithm = TradingAlgorithm.create(
                        type = TradingAlgorithm.Type.TACPP46,
                        history = provider.getBySecurityIdentifier(security).getOrThrow()
                    ).getOrThrow()
                )
                traderService.createTrader(
                    portfolioId = portfolio.id,
                    trader = trader
                )
            }
        }
    }

    //===========================================================//

    fun deleteTraders() {
        scope.launch {
            sellAllHolding()
            val user = userRepository.getById(userId).getOrThrow().toDomain()
            val portfolio = user.portfolio
            portfolio.traders.forEach { trader ->
                traderService.deleteTrader(trader.id)
            }
        }
    }

    //===========================================================//

    fun createTradersByEvalOutput() {
        scope.launch {
            val evalOutput = TradingAlgorithmEvaluator(
                provider = provider,
                tradingAlgorithmType = TradingAlgorithm.Type.TACPP46,
                capital = startCapital,
                taxation = Taxation.Type.Hungary,
                evaluationStartYear = startDate,
                evaluationEndYear = endDate,
                windowStepYears = evaluationWindowStepYears
            ).runEvaluationOnAll().getBestList(10)

            val user = userRepository.getById(userId).getOrThrow().toDomain()
            val portfolio = user.portfolio
            val traders = portfolio.traders
            val traderSecurities = traders.map { trader-> trader.securityIdentifier }
            val notTradedGoodSecurities = evalOutput.filter { security -> security !in traderSecurities }

            notTradedGoodSecurities.forEach { security ->
                val trader = Trader(
                    securityIdentifier = security,
                    allocatedCapital = 20000.0,
                    algorithm = TradingAlgorithm.create(
                        type = TradingAlgorithm.Type.TACPP46,
                        history = provider.getBySecurityIdentifier(security).getOrThrow()
                    ).getOrThrow()
                )
                traderService.createTrader(
                    portfolioId = portfolio.id,
                    trader = trader
                )
            }
        }
    }
}