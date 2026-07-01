package domain.algorithm

import domain.stock.History
import domain.stock.Holding
import java.util.ArrayDeque
import java.util.Deque

//===========================================================//
/**
 * An implementation of [Algorithm].
 */
//===========================================================//

internal class TACPP46(init: Init, emaInit: MutableList<History>) : Algorithm(init) {
    //===========================================================//
    //===========================================================//
    // Private Field(s)

    private val emaHistory: Deque<Double> = ArrayDeque()

    private val trailingHigh: MutableMap<Holding, Double> = HashMap()
    private val markedForSelling: MutableList<Holding> = ArrayList()

    private val lastInputArr: Deque<Double> = ArrayDeque()

    //===========================================================//
    //===========================================================//
    // Public Method(es)

    override fun run(holdings: List<Holding>, allocatedCapital: Double, currentPrice: Double): Output {
        var buy: Output.Buy? = null
        var sell: Output.Sell? = null

        val ema: List<Double> = ArrayList(emaHistory)
        val std: Double = utils.Math.stdDev(ema)
        val rsi: Double = utils.Math.rsi(ema)
        val ma: Double = utils.Math.average(ema)

        val lowerBand = ma - 4.0 * std * ma

        // Buy
        if (rsi <= 30.0 && currentPrice <= lowerBand) {
            if (lastInputArr.isEmpty()) {
                lastInputArr.add(currentPrice)
            } else if (utils.Math.average(ArrayList(lastInputArr)) <= currentPrice) {
                val confidence = Math.clamp(
                    ((1.0 - std * 100.0) + (100.0 - rsi) / 100.0) / 2.0,
                    0.0,
                    0.3
                ) // changing confidence has a massive effect on returns
                val amount = (allocatedCapital * confidence / currentPrice).toLong()

                if (amount != 0L) buy = Output.Buy(amount)
            } else {
                lastInputArr.add(currentPrice)
                if (lastInputArr.size > 5) lastInputArr.poll()
            }
        } else {
            lastInputArr.clear()
        }

        val risk: Double = Math.clamp(std * 100.0, 0.05, 0.2) // to put it into percentages

        // Sell
        val toBeSold: MutableList<Pair<Holding, Long>> = ArrayList()

        // Trailing-profit logic
        for (item in holdings) {
            var isMarked = markedForSelling.contains(item)

            // Activate trailing if gained > risk
            if (!isMarked && currentPrice > item.entryPrice * (1.0 + risk)) {
                markedForSelling.add(item)
                trailingHigh[item] = currentPrice
                isMarked = true
            }

            if (isMarked) {
                var high: Double = trailingHigh.getOrDefault(item, currentPrice)

                // Update trailing high if still rising
                if (currentPrice > high) {
                    high = currentPrice
                    trailingHigh[item] = high
                }

                // Sell if price falls more than risk from peak
                if (currentPrice < high * (1.0 - risk)) {
                    toBeSold.add(Pair(item, item.amount))

                    // cleanup
                    markedForSelling.remove(item)
                    trailingHigh.remove(item)
                }
            }
        }

        // Stop-loss
        for (item in holdings) {
            if (currentPrice < item.entryPrice * (1.0 - risk * 2.0)) {
                toBeSold.add(Pair(item, item.amount))

                // cleanup
                markedForSelling.remove(item)
                trailingHigh.remove(item)
            }
        }

        if (!toBeSold.isEmpty()) sell = Output.Sell(toBeSold)
        return Output(buy, sell)
    }

    //===========================================================//

    override fun updateHistory(history: History) {
        val alpha = 2.0 / (emaHistory.size + 1.0)
        val last = emaHistory.peekLast()

        val newEma = alpha * history.closingPrice + (1.0 - alpha) * last

        emaHistory.pollFirst()
        emaHistory.addLast(newEma)
    }

    //===========================================================//
    //===========================================================//
    // Constructor(s)
    /**
     * Java equivalent of C++ Init::Init_EMA(q0, q1).
     * q0: first slidingWindow prices
     * q1: next slidingWindow prices
     */
    init {
        // SlidingWindow
        val SW = 21
        require(emaInit.size >= 2 * SW) { "Init EMA: not enough history for Initialisation" }

        val historyQ0: List<History>
        val historyQ1: List<History>
        when (init) {
            Init.TRADING -> {
                val n = emaInit.size
                historyQ0 = ArrayList(emaInit.subList(n - 2 * SW, n - SW))
                historyQ1 = ArrayList(emaInit.subList(n - SW, n))
                emaInit.subList(n - 2 * SW, n).clear()
            }

            Init.BACKTEST -> {
                historyQ0 = ArrayList(emaInit.subList(0, SW))
                historyQ1 = ArrayList(emaInit.subList(SW, 2 * SW))
                emaInit.subList(0, 2 * SW).clear()
            }
        }

        val q0 = historyQ0.stream().map { it.closingPrice }.toList()
        val q1 = historyQ1.stream().map { it.closingPrice }.toList()

        val alpha = 2.0 / (q1.size + 1.0)
        var ema = utils.Math.average(q0) // initial Value

        for (price in q1) {
            ema = alpha * price + (1.0 - alpha) * ema
            emaHistory.add(ema)
        }

        check(!emaHistory.isEmpty()) { "EMA is Empty" }
    }
}
