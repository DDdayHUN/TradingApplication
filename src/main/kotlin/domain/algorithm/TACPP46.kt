package domain.algorithm

import domain.assets.security.SecurityHistory
import domain.assets.security.SecurityHolding
import java.util.ArrayDeque
import java.util.Deque

//===========================================================//
/**
 * An implementation of [TradingAlgorithm].
 */
//===========================================================//

internal class TACPP46(init: Init, emaInit: MutableList<SecurityHistory>) : TradingAlgorithm(init) {
    //===========================================================//
    //===========================================================//
    // Private Field(s)

    private val m_EmaHistory: Deque<Double>

    private val m_TrailingHigh: MutableMap<SecurityHolding, Double>
    private val m_MarkedForSelling: MutableList<SecurityHolding>

    private val m_LastInputArr: Deque<Double>

    //===========================================================//
    //===========================================================//
    // Public Method(es)
    
    override fun run(holdings: List<SecurityHolding>, allocatedCapital: Double, currentPrice: Double): Output {
        var buy: Output.Buy? = null
        var sell: Output.Sell? = null

        val ema: List<Double> = ArrayList(m_EmaHistory)
        val std: Double = utils.Math.stdDev(ema)
        val rsi: Double = utils.Math.rsi(ema)
        val ma: Double = utils.Math.average(ema)

        val lowerBand = ma - 4.0 * std * ma

        // Buy
        if (rsi <= 30.0 && currentPrice <= lowerBand) {
            if (m_LastInputArr.isEmpty()) {
                m_LastInputArr.add(currentPrice)
            } else if (utils.Math.average(ArrayList(m_LastInputArr)) <= currentPrice) {
                val confidence = Math.clamp(
                    ((1.0 - std * 100.0) + (100.0 - rsi) / 100.0) / 2.0,
                    0.0,
                    0.3
                ) // changing confidence has a massive effect on returns
                val amount = (allocatedCapital * confidence / currentPrice).toLong()

                if (amount != 0L) buy = Output.Buy(amount)
            } else {
                m_LastInputArr.add(currentPrice)
                if (m_LastInputArr.size > 5) m_LastInputArr.poll()
            }
        } else {
            m_LastInputArr.clear()
        }

        val risk: Double = Math.clamp(std * 100.0, 0.05, 0.2) // to put it into percentages

        // Sell
        val toBeSold: MutableList<Pair<SecurityHolding, Long>> = ArrayList()

        // Trailing-profit logic
        for (item in holdings) {
            var isMarked = m_MarkedForSelling.contains(item)

            // Activate trailing if gained > risk
            if (!isMarked && currentPrice > item.entryPrice * (1.0 + risk)) {
                m_MarkedForSelling.add(item)
                m_TrailingHigh[item] = currentPrice
                isMarked = true
            }

            if (isMarked) {
                var high: Double = m_TrailingHigh.getOrDefault(item, currentPrice)

                // Update trailing high if still rising
                if (currentPrice > high) {
                    high = currentPrice
                    m_TrailingHigh[item] = high
                }

                // Sell if price falls more than risk from peak
                if (currentPrice < high * (1.0 - risk)) {
                    toBeSold.add(Pair(item, item.amount))

                    // cleanup
                    m_MarkedForSelling.remove(item)
                    m_TrailingHigh.remove(item)
                }
            }
        }

        // Stop-loss
        for (item in holdings) {
            if (currentPrice < item.entryPrice * (1.0 - risk * 2.0)) {
                toBeSold.add(Pair(item, item.amount))

                // cleanup
                m_MarkedForSelling.remove(item)
                m_TrailingHigh.remove(item)
            }
        }

        // Update State
        run {
            val alpha = 2.0 / (m_EmaHistory.size + 1.0)
            val last = m_EmaHistory.peekLast()

            val newEma = alpha * currentPrice + (1.0 - alpha) * last

            m_EmaHistory.pollFirst()
            m_EmaHistory.addLast(newEma)
        }

        if (!toBeSold.isEmpty()) sell = Output.Sell(toBeSold)
        return Output(buy, sell)
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
        m_EmaHistory = ArrayDeque()

        m_TrailingHigh = HashMap()
        m_MarkedForSelling = ArrayList()

        m_LastInputArr = ArrayDeque()

        // SlidingWindow
        val SW = 21
        require(emaInit.size >= 2 * SW) { "Init EMA: not enough history for Initialisation" }

        val historyQ0: List<SecurityHistory>
        val historyQ1: List<SecurityHistory>
        when (init) {
            Init.TRADING -> {
                val n = emaInit.size
                historyQ0 = emaInit.subList(n - 2 * SW, n - SW).toList()
                historyQ1 = emaInit.subList(n - SW, n).toList()
                emaInit.subList(n - 2 * SW, n).clear()
            }

            Init.BACKTEST -> {
                historyQ0 = emaInit.subList(0, SW).toList()
                historyQ1 = emaInit.subList(SW, 2 * SW).toList()
                emaInit.subList(0, 2 * SW).clear()
            }
        }

        val q0 = historyQ0.stream().map { it.closingPrice }.toList()
        val q1 = historyQ1.stream().map { it.closingPrice }.toList()

        val alpha = 2.0 / (q1.size + 1.0)
        var ema = utils.Math.average(q0) // initial Value

        for (price in q1) {
            ema = alpha * price + (1.0 - alpha) * ema
            m_EmaHistory.add(ema)
        }

        check(!m_EmaHistory.isEmpty()) { "EMA is Empty" }
    }
}
