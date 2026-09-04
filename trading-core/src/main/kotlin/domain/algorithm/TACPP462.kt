package domain.algorithm

import domain.market.security.SecurityHistory
import domain.market.security.SecurityHolding
import domain.utils.Math.rsi
import domain.utils.Math.stdDev
import java.util.*

//===========================================================//
/**
 * An implementation of [TradingAlgorithm].
 */
//===========================================================//

internal class TACPP462: ITradingAlgorithm {
    //===========================================================//
    //===========================================================//
    // Private Field(s)

    private val m_SlidingWindow = 21

    private val m_EmaHistory: Deque<Double>

    private val m_TrailingHigh: MutableMap<UUID, Double>
    private val m_MarkedForSelling: MutableList<UUID>

    private val m_LastInputArr: Deque<Double>

    //===========================================================//
    //===========================================================//
    // Public Method(es)

    override fun run(holdings: Set<SecurityHolding>, allocatedCapital: Double, currentPrice: Double): TradingAlgorithm.Output {
        var buy: TradingAlgorithm.Output.Buy? = null
        var sell: TradingAlgorithm.Output.Sell? = null

        val ema: List<Double> = ArrayList(m_EmaHistory)
        val std = ema.stdDev()
        val rsi = ema.rsi()
        val ma = ema.average()

        val lowerBand = ma - 4.0 * std
        val confidence = (((1.0 - std * 100.0) + (100.0 - rsi) / 100.0) / 2.0).coerceIn(0.0, 0.5)

        // Buy
        if (rsi <= 50.0 && currentPrice <= lowerBand) {
            if (m_LastInputArr.isEmpty()) {
                m_LastInputArr.add(currentPrice)
            } else if (ArrayList(m_LastInputArr).average() <= currentPrice) {
                val amount = (allocatedCapital * confidence / currentPrice).toInt()
                if (amount != 0) buy = TradingAlgorithm.Output.Buy(amount)
            } else {
                m_LastInputArr.add(currentPrice)
                if (m_LastInputArr.size > 5) m_LastInputArr.poll()
            }
        } else {
            m_LastInputArr.clear()
        }

        // Sell
        val toBeSold: MutableSet<Pair<SecurityHolding, Int>> = HashSet()

        // Trailing-profit logic
        for (item in holdings) {
            var isMarked = m_MarkedForSelling.contains(item.id)

            // Activate trailing if gained > risk
            if (!isMarked && currentPrice > item.purchasePrice * (1 + std)) {
                m_MarkedForSelling.add(item.id)
                m_TrailingHigh[item.id] = currentPrice
                isMarked = true
            }

            if (isMarked) {
                var high: Double = m_TrailingHigh.getOrDefault(item.id, currentPrice)

                // Update trailing high if still rising
                if (currentPrice > high) {
                    high = currentPrice
                    m_TrailingHigh[item.id] = high
                }

                // Sell if price falls more than risk from peak
                if (currentPrice < high * (1.0 - std)) {
                    val pair = Pair(item, item.amount)
                    if(!toBeSold.contains(pair)) toBeSold.add(pair)

                    // cleanup
                    m_MarkedForSelling.remove(item.id)
                    m_TrailingHigh.remove(item.id)
                }
            }
        }

        // Stop-loss
        for (item in holdings) {
            if (currentPrice < item.purchasePrice * (1.0 - std * 2)) {
                val pair = Pair(item, item.amount)
                if(!toBeSold.contains(pair)) toBeSold.add(pair)

                // cleanup
                m_MarkedForSelling.remove(item.id)
                m_TrailingHigh.remove(item.id)
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
        var ema = q0.average() // initial Value

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