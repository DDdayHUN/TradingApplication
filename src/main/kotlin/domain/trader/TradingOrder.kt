package domain.trader

import domain.algorithm.TradingAlgorithm
import domain.market.security.SecurityIdentifier
import java.time.Instant
import java.util.UUID

//===========================================================//
/**
 * Represents formatted trading signal that can be displayed
 */
//===========================================================//

data class TradingOrder(
    val orderUuid: UUID = UUID.randomUUID(),
    val traderUuid: UUID,
    val securityIdentifier: SecurityIdentifier,
    val buy: TradingAlgorithm.Output.Buy?,
    val sell: TradingAlgorithm.Output.Sell?,
    val atPrice: Double,
    val createdAt: Instant = Instant.now(),
) {
    //===========================================================//
    //===========================================================//
    // Public Method(es)

    fun toReadableText(): String {
        val action = if(buy == null && sell == null) "HOLD"
        else if(buy != null && sell != null) "BUY, SELL"
        else if(buy != null) "BUY" else "SELL"

        val amount = if(buy == null && sell == null) ""
        else if(buy != null && sell != null) " | Buy Amount: ${buy.amount} | Sell: ${sell.batches.map { it.first }.toList()}"
        else if(buy != null) " | Buy Amount: ${buy.amount}" else " | Sell: ${sell!!.batches.map { it.first }.toList()}}"

        return ("" +
                action
                + " | At Price: "
                + String.format("%.2f", atPrice)
                + amount
                + " | At: "
                + createdAt)
    }
}