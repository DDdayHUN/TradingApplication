package infrastructure.broker

import application.logging.logger
import application.service.portfolio.IPortfolioService
import application.service.trader.ITraderService
import data.network.ibkr.backtest.BacktestDataService
import domain.market.security.SecurityIdentifier
import application.service.broker.InteractiveBrokersOrder
import application.service.broker.InteractiveBrokersOrderService
import application.service.broker.toInteractiveBrokersOrder
import domain.algorithm.TradingAlgorithm
import domain.trader.TradingOrder
import kotlinx.coroutines.*
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.util.*
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days

@Deprecated("ONLY TESTING")
@Component
class Test(
    private val orderService: InteractiveBrokersOrderService,
    private val traderService: ITraderService,
    private val portfolioService: IPortfolioService,
    private val backtestDataService: BacktestDataService
) {
    private val logger = logger<Test>()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val portfolioId = UUID.fromString("07cd85e1-8e40-4fe7-ba4a-8959344b9259")

    @Scheduled(
        cron = "*/4 * * * * *",
        zone = "Europe/Budapest"
    )
    fun placeConcurrentTestOrders() {
        scope.launch {
            try {
                val portfolio = portfolioService.getPortfolio(portfolioId)

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

    fun getHistoricalData(){
        scope.launch {
            val identifier = SecurityIdentifier(
                isin = "US0378331005",
                tickerSymbol = "AAPL",
                currency = "USD",
            )

            val to = Clock.System.now()
            val from = to - (365.days)

            backtestDataService.download(identifier, from, to)
        }
    }

    @Scheduled(
        cron = "0 12 19 * * *",
        zone = "Europe/Budapest"
    )
    fun sellAllHolding() {
        scope.launch {
            val portfolio = portfolioService.getPortfolio(portfolioId)

            portfolio.traders.forEach { trader ->

                val orders = traderService.forceSellAllHolding(
                    trader.id
                )

                orders.forEach { order ->
                    orderService.submit(order)
                }
            }
        }
    }

    @Scheduled(
        cron = "0 11 19 * * *",
        zone = "Europe/Budapest"
    )
    fun buyHolding(){
        scope.launch {
            try {
                val portfolio = portfolioService.getPortfolio(portfolioId)

                portfolio.traders.forEach { trader ->
                        val order = TradingOrder(
                            traderId = trader.id,
                            signal = TradingAlgorithm.Output(TradingAlgorithm.Output.Buy(amount = 3), null),
                            atPrice = 433.0,
                        )
                        orderService.submit(order)
                }
            } catch(e: Exception){
                throw e
            }
        }
    }
}