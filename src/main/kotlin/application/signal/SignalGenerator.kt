package application.signal

import domain.algorithm.TradingAlgorithm
import domain.signal.TradingSignal
import java.time.Instant

//===========================================================//
/**
 * Converts algorithm outputs [TradingAlgorithm.Output] into final trading signals [TradingSignal].
 */
//===========================================================//

class SignalGenerator {
    //===========================================================//
    //===========================================================//
    // Public Method(es)

    fun createSignal(symbol: String, output: TradingAlgorithm.Output, availableCapital: Double, currentPrice: Double, currentStockCount: Long): List<TradingSignal> {
        val ret: MutableList<TradingSignal> = ArrayList()

        if (output.buy != null) {
            ret.add(
                TradingSignal(
                    symbol,
                    TradingSignal.Action.BUY,
                    calculateBuyStrength(
                        output.buy.amount,
                        availableCapital,
                        currentPrice
                    ),
                    currentPrice,
                    output.buy.amount,
                    currentStockCount,
                    "Algorithm generated a buy signal.",
                    Instant.now()
                )
            )
        }

        if (output.sell != null) {
            val amountToSell: Long = output.sell
                .batches
                .stream()
                .mapToLong { batch -> batch.second }
                .sum()

            ret.add(
                TradingSignal(
                    symbol,
                    TradingSignal.Action.SELL,
                    TradingSignal.Strength.MEDIUM,
                    currentPrice,
                    amountToSell,
                    currentStockCount,
                    "Algorithm generated a sell signal.",
                    Instant.now()
                )
            )
        }

        if (!ret.isEmpty()) return ret

        ret.add(
            TradingSignal(
                symbol,
                TradingSignal.Action.HOLD,
                TradingSignal.Strength.LOW,
                currentPrice,
                null,
                currentStockCount,
                "No buy or sell signal was generated.",
                Instant.now()
            )
        )

        return ret
    }

    //===========================================================//
    //===========================================================//
    // Private Method(es)

    private fun calculateBuyStrength(amount: Long, availableCapital: Double, currentPrice: Double): TradingSignal.Strength {
        if (availableCapital <= 0.0) return TradingSignal.Strength.LOW

        val usedCapital = amount * currentPrice
        val usedCapitalRatio = usedCapital / availableCapital

        if (usedCapitalRatio >= 0.20) return TradingSignal.Strength.HIGH
        if (usedCapitalRatio >= 0.10) return TradingSignal.Strength.MEDIUM
        return TradingSignal.Strength.LOW
    }
}