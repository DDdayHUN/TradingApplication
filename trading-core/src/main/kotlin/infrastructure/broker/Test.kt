package infrastructure.broker

import application.logging.logger
import application.service.order.IOrderService
import application.service.portfolio.IPortfolioService
import application.service.trader.ITraderService
import data.network.ibkr.backtest.BacktestDataService
import domain.market.security.SecurityIdentifier
import domain.order.Order
import exception.api.TraderNotFoundException
import kotlinx.coroutines.*
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.util.*
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days

@Deprecated("ONLY TESTING")
@Component
class Test(
    private val orderService: IOrderService,
    private val traderService: ITraderService,
    private val portfolioService: IPortfolioService,
    private val backtestDataService: BacktestDataService
) {

    private val logger = logger<Test>()
    private val scope = CoroutineScope(
        SupervisorJob() + Dispatchers.IO
    )

    private val portfolioId =
        UUID.fromString("73676208-6428-44e0-898f-4368d551df2c")


    @Scheduled(
        cron = "0 54 21 * * *",
        zone = "Europe/Budapest"
    )
    fun placeConcurrentTestOrders() {
        scope.launch {
            try {

                val portfolio =
                    portfolioService.getPortfolio(portfolioId)

                val jobs = portfolio.traders.map { trader ->
                    launch {
                        try {
                            val order = traderService.executeTrader(trader.id)

                            logger.info(
                                "Submitting trader={} order={}",
                                trader.securityIdentifier.tickerSymbol,
                                order.toString()
                            )

                            if(order != null) orderService.submit(order)

                        } catch (e: Exception) {
                            logger.error(
                                "Failed trader={}",
                                trader.id,
                                e
                            )
                        }
                    }
                }

                jobs.joinAll()

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
        cron = "0 25 17 * * *",
        zone = "Europe/Budapest"
    )
    fun sellAllHolding(){
        scope.launch {
            val portfolio = portfolioService.getPortfolio(portfolioId)
            val jobs =  portfolio.traders.map { trader ->
                launch {
                    try {
                        traderService.forceSellAllHolding(trader.id).forEach { order ->
                            logger.info(
                                "Submitting trader={} order={}",
                                trader.securityIdentifier.tickerSymbol,
                                order.toString()
                            )
                            orderService.submit(order)
                        }
                    }catch(e: Exception){
                        throw e
                    }
                }
            }
            jobs.joinAll()
        }
    }

    @Scheduled(
        cron = "0 54 21 * * *",
        zone = "Europe/Budapest"
    )
    fun buyHolding(){
        scope.launch {
            try {
                val portfolio = portfolioService.getPortfolio(portfolioId)
                val traderId = UUID.fromString("d93f4ea1-4abb-42e0-a28b-648957b81388")

                val trader = portfolio.traders.find { trader ->
                    traderId == trader.id
                } ?: throw TraderNotFoundException(traderId)

                val order = Order(
                    traderId = trader.id,
                    securityIdentifier = trader.securityIdentifier,
                    signal = Order.Signal.Buy(amount = 3),
                    signalPrice = 433.0,
                )

                orderService.submit(order)
            } catch(e: Exception){
                throw e
            }

        }
    }
}