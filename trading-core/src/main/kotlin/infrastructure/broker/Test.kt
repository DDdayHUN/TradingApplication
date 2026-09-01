package infrastructure.broker

import application.logging.logger
import application.service.IOrderService
import application.service.ITraderService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.util.*

@Deprecated("ONLY TESTING")
@Component
class Test(
    private val orderService: IOrderService,
    private val traderService: ITraderService
) {

    private val logger = logger<Test>()
    private val scope = CoroutineScope(
        SupervisorJob() + Dispatchers.IO
    )

    @Scheduled(
        cron = "0 06 16 * * *",
        zone = "Europe/Budapest"
    )
    fun placeNvdaTestOrder() {
        scope.launch {
            try {
                val portfolioId = UUID.fromString("8f69dedf-d2c1-4ac5-846e-639ef99603cf")
                val traderId = UUID.fromString("43bf3b43-6b10-46af-8a63-60266d2d4322")

                val order = traderService.executeTrader(portfolioId, traderId)
                logger.info("TRADING ORDER: ${order.toReadableText()}")
                logger.info("QUOTE: ${order.atPrice}")

                orderService.submit(order)

            } catch (e: Exception) {
                logger.error(
                    "TEST: failed to submit NVDA order",
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