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
    constructor(output: TradingAlgorithm.Output, currentPrice: Double) : this(
        buy = output.buy,
        sell = output.sell,
        currentPrice = currentPrice
    )
}
