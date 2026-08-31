package infrastructure.broker

import application.logging.logger
import application.service.IOrderService
import application.service.ITraderService
import data.network.MarketDataProvider
import domain.algorithm.TradingAlgorithm
import domain.market.security.SecurityIdentifier
import domain.trader.TradingOrder
import exception.api.TraderNotFoundException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.util.UUID
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
        cron = "0 22 22 * * *",
        zone = "Europe/Budapest"
    )
    fun placeNvdaTestOrder() {
        scope.launch {
            try {
                val portfolioId = UUID.fromString("567f34ad-8b05-44ac-a944-05adfb9fe897")
                val traderId = UUID.fromString("47c11a67-92ea-481d-b858-00b12aff009b")

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