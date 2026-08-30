package infrastructure.broker

import application.logging.logger
import application.service.IOrderService
import domain.algorithm.TradingAlgorithm
import domain.market.security.SecurityIdentifier
import domain.trader.TradingOrder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class Test(
    private val orderService: IOrderService
) {

    private val logger = logger<Test>()

    private val scope = CoroutineScope(
        SupervisorJob() + Dispatchers.IO
    )

    @Scheduled(
        cron = "0 58 10 * * *",
        zone = "Europe/Budapest"
    )
    fun placeNvdaTestOrder() {
        scope.launch {
            try {
                logger.info("TEST: submitting BUY 10 NVDA")

                val order = TradingOrder(
                    traderId = UUID.fromString(
                        "47c11a67-92ea-481d-b858-00b12aff009b"
                    ),
                    securityIdentifier = SecurityIdentifier(
                        isin = "US67066G1040",
                        tickerSymbol = "NVDA",
                        currency = "USD"
                    ),
                    buy = TradingAlgorithm.Output.Buy(
                        amount = 10
                    ),
                    sell = null,
                    atPrice = 180.0
                )

                orderService.submit(order)

                logger.info(
                    "TEST: NVDA order passed to OrderService"
                )

            } catch (e: Exception) {
                logger.error(
                    "TEST: failed to submit NVDA order",
                    e
                )
            }
        }
    }
}