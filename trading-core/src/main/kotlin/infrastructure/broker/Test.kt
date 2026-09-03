package infrastructure.broker

import application.logging.logger
import application.service.order.IOrderService
import application.service.portfolio.IPortfolioService
import application.service.trader.ITraderService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.util.*

@Deprecated("ONLY TESTING")
@Component
class Test(
    private val orderService: IOrderService,
    private val traderService: ITraderService,
    private val portfolioService: IPortfolioService
) {

    private val logger = logger<Test>()
    private val scope = CoroutineScope(
        SupervisorJob() + Dispatchers.IO
    )

    @Scheduled(
        cron = "0 */1 * * * *",
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



    //@Scheduled(
    //   cron = "*/10 * * * * *",
    //    zone = "Europe/Budapest"
    //)
    /*
    private suspend fun getFinnhubQuote() {
        scope.launch{
            try{
                val provider = MarketDataProvider.create(MarketDataProvider.Type.Finnhub)
                val identifier = SecurityIdentifier(
                    isin = "US67066G1040",
                    tickerSymbol = "NVDA",
                    currency = "USD",
                )
                val quote = provider.getQuote(identifier).getOrThrow()

                logger.info("Current Price for ${identifier.tickerSymbol} : ${quote.currentPrice}")
            } catch (e: Exception) {
                logger.error("Failed to get Finnhub Quote}", e)
            }
        }
    }
    */

}