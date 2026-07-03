package domain.signal

import domain.algorithm.TradingAlgorithm
import java.time.Instant

//===========================================================//
/**
 * Represents formatted trading signal that can be displayed
 */
//===========================================================//

class TradingSignal private constructor(
    val buy: TradingAlgorithm.Output.Buy?,
    val sell: TradingAlgorithm.Output.Sell?,
    val currentPrice: Double,
    val createdAt: Instant = Instant.now()
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
                + " | Price: "
                + String.format("%.2f", currentPrice)
                + amount
                + " | At: "
                + createdAt)
    }

    //===========================================================//
    //===========================================================//
    // Constructor(s)

    constructor(output: TradingAlgorithm.Output, currentPrice: Double) : this(
        buy = output.buy,
        sell = output.sell,
        currentPrice = currentPrice
    )
}
