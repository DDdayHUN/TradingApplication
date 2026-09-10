package domain.trader

import domain.algorithm.TradingAlgorithm
import domain.market.security.SecurityIdentifier
import java.time.Instant
import java.util.*

//===========================================================//
/**
 * Represents formatted trading signal that can be displayed
 */
//===========================================================//
@Deprecated("Deprecated by domain.order")
data class TradingOrder(
    val orderId: UUID = UUID.randomUUID(),
    val traderId: UUID,
    val securityIdentifier: SecurityIdentifier,
    val buy: TradingAlgorithm.Output.Buy?,
    val sell: TradingAlgorithm.Output.Sell?,
    val atPrice: Double,
    val createdAt: Instant = Instant.now(),
) {

}