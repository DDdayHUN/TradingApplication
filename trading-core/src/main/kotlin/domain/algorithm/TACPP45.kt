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

internal class TACPP45 : TradingAlgorithm {
    //===========================================================//
    //===========================================================//
    // Private Field(s)

    private val m_SlidingWindow = 28

    private val m_EmaHistory: Deque<Double>

    private val m_TrailingHigh: MutableMap<UUID, Double>
    private val m_MarkedForSelling: MutableMap<UUID, Int>

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

        val lower_band = ma - 8.0 * std * ma

        // Buy
        if (rsi <= 40 && currentPrice <= lower_band) {
            val confidence = (((1 - std * 100) + (100.0f - rsi) / 100.0f) / 2.0f).coerceIn(0.0, 0.5)
            val amount = (allocatedCapital * confidence / currentPrice).toInt()

            if(amount > 0) buy = TradingAlgorithm.Output.Buy(amount)
        }

        val risk = (std * 100).coerceIn(0.05, 0.2)

        // Sell
        val toBeSold: MutableSet<Pair<SecurityHolding, Int>> = HashSet()
        for (item in holdings) {
            // Activate trailing if gained >30%
            var isMarked = m_MarkedForSelling.contains(item.id)

            if (!isMarked && currentPrice > item.purchasePrice * 1.3f) {
                m_MarkedForSelling[item.id] = item.amount
                m_TrailingHigh[item.id] = currentPrice
                isMarked = true
            }

            // Update trailing high if still rising
            if (isMarked) {
                var high = m_TrailingHigh.getOrDefault(item.id, currentPrice)

                if (currentPrice > high) m_TrailingHigh[item.id] = high

                // Sell if price falls more than 10 from peak
                if (currentPrice < high * (1.0f - risk)) toBeSold.add(Pair(item, item.amount))
            }
        }

        // Stop loss
        for (item in holdings) {
            if (currentPrice < item.purchasePrice * (1.0f - risk * 2.0f)) {
                toBeSold.add(Pair(item, item.amount))
            }
        }

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

        check(m_EmaHistory.size == m_SlidingWindow) { "EMA" }
    }

    //===========================================================//

    init {
        m_EmaHistory = ArrayDeque()

        m_TrailingHigh = HashMap()
        m_MarkedForSelling = HashMap()
    }
}