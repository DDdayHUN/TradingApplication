package domain.algorithm

import domain.assets.security.SecurityHistory
import domain.assets.security.SecurityHolding
import java.util.ArrayDeque
import java.util.Deque

internal class ALGDES1 : ITradingAlgorithm {
    //===========================================================//
    //===========================================================//
    // Private Field(s)

    private val m_MWSize1 = 15
    private val m_MWSize2 = 25

    private val m_MovingWindow1: Deque<Double>
    private val m_MovingWindow2: Deque<Double>

    private val m_TrailingHigh: MutableMap<SecurityHolding, Double>
    private val m_MarkedForSelling: MutableList<SecurityHolding>

    //===========================================================//
    //===========================================================//
    // Public Method(es)

    override fun run(holdings: List<SecurityHolding>, allocatedCapital: Double, currentPrice: Double): TradingAlgorithm.Output {
        var buy: TradingAlgorithm.Output.Buy? = null
        var sell: TradingAlgorithm.Output.Sell? = null

        val mw1 = utils.Math.average(m_MovingWindow1.toList())
        val mw2 = utils.Math.average(m_MovingWindow2.toList())

        // Buy
        if(2 * mw1 > mw2) {
            val amount = allocatedCapital.div(currentPrice).toLong()
            if(amount > 0) buy = TradingAlgorithm.Output.Buy(amount)
        }

        // Sell
        val toBeSold: MutableList<Pair<SecurityHolding, Long>> = ArrayList()

        // Trailing-profit logic
        for (item in holdings) {
            var isMarked = m_MarkedForSelling.contains(item)

            if (!isMarked && currentPrice > item.entryPrice * 1.1) {
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

                // Sell if price falls from high
                if (currentPrice < high * 0.95) {
                    toBeSold.add(Pair(item, item.amount))

                    // cleanup
                    m_MarkedForSelling.remove(item)
                    m_TrailingHigh.remove(item)
                }
            }
        }

        // Stop-loss
        for (item in holdings) {
            if (currentPrice < item.entryPrice * 1.1) {
                toBeSold.add(Pair(item, item.amount))

                // cleanup
                m_MarkedForSelling.remove(item)
                m_TrailingHigh.remove(item)
            }
        }

        // Update State
        run {
            m_MovingWindow1.addLast(currentPrice)
            m_MovingWindow2.addLast(m_MovingWindow1.first)
            m_MovingWindow1.removeFirst()
            m_MovingWindow2.removeFirst()
        }

        if (!toBeSold.isEmpty()) sell = TradingAlgorithm.Output.Sell(toBeSold)
        return TradingAlgorithm.Output(
            buy = buy,
            sell = sell
        )
    }

    //===========================================================//
    //===========================================================//
    // Constructor(s)

    constructor(history: List<SecurityHistory>) {
        require(m_MWSize1 + m_MWSize2 == history.size) { "Init" }

        val h1 = history.subList(history.size - m_MWSize1, history.size).map { it.closingPrice }
        val h2 = history.subList(0, m_MWSize2).map { it.closingPrice }

        m_MovingWindow1.addAll(h1)
        m_MovingWindow2.addAll(h2)

        check(m_MovingWindow1.size == m_MWSize1 && m_MovingWindow2.size == m_MWSize2) { "Init 2" }
    }

    //===========================================================//

    init {
        m_MovingWindow1 = ArrayDeque()
        m_MovingWindow2 = ArrayDeque()

        m_TrailingHigh = HashMap()
        m_MarkedForSelling = ArrayList()
    }
}