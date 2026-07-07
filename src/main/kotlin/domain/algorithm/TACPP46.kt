package domain.algorithm

import domain.market.security.SecurityHistory
import domain.market.security.SecurityHolding
import java.util.ArrayDeque
import java.util.Deque

//===========================================================//
/**
 * An implementation of [TradingAlgorithm].
 */
//===========================================================//

internal class TACPP46: ITradingAlgorithm {
    //===========================================================//
    //===========================================================//
    // Private Field(s)

    private val m_SlidingWindow = 21

    private val m_EmaHistory: Deque<Double>

    private val m_TrailingHigh: MutableMap<SecurityHolding, Double>
    private val m_MarkedForSelling: MutableList<SecurityHolding>

    private val m_LastInputArr: Deque<Double>

    //===========================================================//
    //===========================================================//
    // Public Method(es)

    override fun run(holdings: List<SecurityHolding>, allocatedCapital: Double, currentPrice: Double): TradingAlgorithm.Output {
        var buy: TradingAlgorithm.Output.Buy? = null
        var sell: TradingAlgorithm.Output.Sell? = null

        val ema: List<Double> = ArrayList(m_EmaHistory)
        val std: Double = domain.utils.Math.stdDev(ema)
        val rsi: Double = domain.utils.Math.rsi(ema)
        val ma: Double = domain.utils.Math.average(ema)

        val lowerBand = ma - 4.0 * std * ma

        // Buy
        if (rsi <= 30.0 && currentPrice <= lowerBand) {
            if (m_LastInputArr.isEmpty()) {
                m_LastInputArr.add(currentPrice)
            } else if (domain.utils.Math.average(ArrayList(m_LastInputArr)) <= currentPrice) {
                val confidence = Math.clamp(
                    ((1.0 - std * 100.0) + (100.0 - rsi) / 100.0) / 2.0,
                    0.0,
                    0.3
                ) // changing confidence has a massive effect on returns
                val amount = (allocatedCapital * confidence / currentPrice).toInt()

                if (amount != 0) buy = TradingAlgorithm.Output.Buy(amount)
            } else {
                m_LastInputArr.add(currentPrice)
                if (m_LastInputArr.size > 5) m_LastInputArr.poll()
            }
        } else {
            m_LastInputArr.clear()
        }

        val risk: Double = Math.clamp(std * 100.0, 0.05, 0.2) // to put it into percentages

        // Sell
        val toBeSold: MutableList<Pair<SecurityHolding, Int>> = ArrayList()

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

        if (!toBeSold.isEmpty()) sell = TradingAlgorithm.Output.Sell(toBeSold)
        return TradingAlgorithm.Output(buy, sell)
    }

    //===========================================================//
    //===========================================================//
    // Constructor(s)

    /**
     * Java equivalent of C++ Init::Init_EMA(q0, q1).
     * q0: first slidingWindow prices
     * q1: next slidingWindow prices
     */
    constructor(emaInit: List<SecurityHistory>) {
        require(emaInit.size >= 2 * m_SlidingWindow) { "Init EMA" }

        val historyQ0 = emaInit.subList(0, m_SlidingWindow).toList()
        val historyQ1 = emaInit.subList(m_SlidingWindow, 2 * m_SlidingWindow).toList()

        val q0 = historyQ0.stream().map { it.closingPrice }.toList()
        val q1 = historyQ1.stream().map { it.closingPrice }.toList()

        val alpha = 2.0 / (q1.size + 1.0)
        var ema = domain.utils.Math.average(q0) // initial Value

        for (price in q1) {
            ema = alpha * price + (1.0 - alpha) * ema
            m_EmaHistory.add(ema)
        }

        check(m_EmaHistory.size == 21) { "EMA" }
    }

    //===========================================================//

    init {
        m_EmaHistory = ArrayDeque()

        m_TrailingHigh = HashMap()
        m_MarkedForSelling = ArrayList()

        m_LastInputArr = ArrayDeque()
    }
}