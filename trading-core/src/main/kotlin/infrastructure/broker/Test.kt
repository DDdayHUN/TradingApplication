package infrastructure.broker

import application.logging.logger
import application.service.IOrderService
import application.service.ITraderService
import domain.algorithm.TradingAlgorithm
import domain.trader.TradingOrder
import exception.api.TraderNotFoundException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.util.UUID

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
        cron = "* 23 23 * * *",
        zone = "Europe/Budapest"
    )
    fun placeNvdaTestOrder() {
        scope.launch {
            try {
                val portfolioId = UUID.fromString("567f34ad-8b05-44ac-a944-05adfb9fe897")
                val traderId = UUID.fromString("47c11a67-92ea-481d-b858-00b12aff009b")

                val order = traderService.executeTrader(portfolioId, traderId)
                logger.info("TRADING ORDER: ${order.toReadableText()}")

                orderService.submit(order)

            } catch (e: Exception) {
                logger.error(
                    "TEST: failed to submit NVDA order",
                    e
                )
            }
        }
    }

    @Scheduled(
        cron = "0 0 16 * * *",
        zone = "Europe/Budapest"
    )
    fun testForcedBuyProcess() {
        scope.launch {
            try {
                val userId =
                    UUID.fromString("f0792158-24a0-427f-999d-6cf8fa3a0cf3")

                val portfolioId =
                    UUID.fromString("567f34ad-8b05-44ac-a944-05adfb9fe897")

                val traderId =
                    UUID.fromString("47c11a67-92ea-481d-b858-00b12aff009b")

                val trader = traderService.getById(
                    userId = userId,
                    portfolioId = portfolioId,
                    traderId = traderId
                ) ?: throw TraderNotFoundException(traderId)

                val order = TradingOrder(
                    traderId = trader.id,
                    securityIdentifier = trader.securityIdentifier,
                    buy = TradingAlgorithm.Output.Buy(
                        amount = 10
                    ),
                    sell = null,
                    atPrice = 0.0
                )

                logger.info(
                    "FORCED BUY TEST: {}",
                    order.toReadableText()
                )

                orderService.submit(order)

            } catch (e: Exception) {
                logger.error(
                    "FORCED BUY TEST failed",
                    e
                )
            }
        }
    }
}