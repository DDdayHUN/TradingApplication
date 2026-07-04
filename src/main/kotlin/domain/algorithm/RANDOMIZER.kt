package domain.algorithm

import domain.assets.security.SecurityHolding
import kotlin.random.Random

/**
 * AI Made, so we will see, I barely touched it XDD aaaand its garbage XDDDD
 */
internal class RANDOMIZER : ITradingAlgorithm {
    override fun run(holdings: List<SecurityHolding>, allocatedCapital: Double, currentPrice: Double): TradingAlgorithm.Output {
        var buy: TradingAlgorithm.Output.Buy? = null
        var sell: TradingAlgorithm.Output.Sell? = null

        // Statistics
        val totalAmount = holdings.sumOf { it.amount }

        val averageEntry =
            if (holdings.isEmpty()) {
                currentPrice
            } else {
                holdings.sumOf { it.entryPrice * it.amount } / totalAmount.toDouble()
            }

        val random = Random.nextDouble()

        val priceDifference =
            if (averageEntry == 0.0) {
                0.0
            } else {
                (currentPrice - averageEntry) / averageEntry
            }

        //=========================================================
        // Buy

        val buyChance = when {
            priceDifference < -0.10 -> 0.70
            priceDifference < -0.05 -> 0.50
            else -> 0.20
        }

        if (allocatedCapital >= currentPrice && random < buyChance) {

            val confidence = Random.nextDouble(0.05, 0.30)

            val amount = (allocatedCapital * confidence / currentPrice).toLong()

            if (amount > 0L) {
                buy = TradingAlgorithm.Output.Buy(amount)
            }
        }

        //=========================================================
        // Sell

        val toBeSold = mutableListOf<Pair<SecurityHolding, Long>>()

        for (holding in holdings) {
            val profit = (currentPrice - holding.entryPrice) / holding.entryPrice

            val sellChance = when {
                profit > 0.20 -> 0.80
                profit > 0.10 -> 0.50
                profit > 0.00 -> 0.30
                profit < -0.15 -> 0.60   // panic sell
                else -> 0.10
            }

            if (Random.nextDouble() < sellChance) {

                val amount = Random.nextLong(
                    1,
                    holding.amount + 1
                )

                toBeSold.add(holding to amount)
            }
        }

        //=========================================================
        // Output

        if (toBeSold.isNotEmpty()) {
            sell = TradingAlgorithm.Output.Sell(toBeSold)
        }

        return TradingAlgorithm.Output(
            buy = buy,
            sell = sell
        )
    }
}