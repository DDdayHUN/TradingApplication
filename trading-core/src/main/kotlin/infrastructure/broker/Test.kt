package infrastructure.broker

import application.logging.logger
import application.service.order.IOrderService
import application.service.portfolio.IPortfolioService
import application.service.trader.ITraderService
import data.network.ibkr.backtest.BacktestDataService
import domain.market.security.SecurityIdentifier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
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

    @Scheduled(
        cron = "0 */2 * * * *",
        zone = "Europe/Budapest"
    )
    fun placeConcurrentTestOrders() {
        scope.launch {
            try {
                val portfolioId =
                    UUID.fromString("73676208-6428-44e0-898f-4368d551df2c")

                val portfolio =
                    portfolioService.getPortfolio(portfolioId)

                val jobs = portfolio.traders.map { trader ->
                    launch {
                        try {
                            val order =
                                traderService.executeTrader(
                                    portfolioId,
                                    trader.id
                                )

                            logger.info(
                                "Submitting trader={} order={}",
                                trader.securityIdentifier.tickerSymbol,
                                order.toReadableText()
                            )

                            orderService.submit(order)

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



    @Scheduled(
        cron = "0 18 19 * * *",
        zone = "Europe/Budapest"
    )
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

}