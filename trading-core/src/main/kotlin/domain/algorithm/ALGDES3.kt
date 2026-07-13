package domain.algorithm

import domain.market.security.SecurityHistory
import domain.market.security.SecurityHolding
import java.util.ArrayDeque
import java.util.Deque

internal class ALGDES3 : ITradingAlgorithm {
    //===========================================================//
    //===========================================================//
    // Private Field(s)

    private val m_MWSize = 15
    private val m_MovingWindow: Deque<Double>

    //===========================================================//
    //===========================================================//
    // Public Method(es)

    override fun run(holdings: List<SecurityHolding>, allocatedCapital: Double, currentPrice: Double): TradingAlgorithm.Output {
        var buy: TradingAlgorithm.Output.Buy? = null
        var sell: TradingAlgorithm.Output.Sell? = null

        val history = m_MovingWindow.toList()

        val mean = history.average()
        val std = domain.utils.Math.stdDev(history)
        val risk = Math.clamp(std * 100.0, 0.1, 0.3)

        val lowerBand = mean - std
        val upperBand = mean + std

        //-------------------------------------------------------
        // Buy

        if (currentPrice < lowerBand) {
            val amount = (allocatedCapital * Math.clamp(1.div(risk), 0.0, 1.0) / currentPrice).toInt()

            if (amount > 0) buy = TradingAlgorithm.Output.Buy(amount)
        }

        //-------------------------------------------------------
        // Sell

        val toSell = mutableListOf<Pair<SecurityHolding, Int>>()

        for (holding in holdings) {
            val gain = (currentPrice - holding.entryPrice) / holding.entryPrice

            when {
                // Price is high relative to average
                currentPrice > 2 * upperBand -> toSell.add(holding to holding.amount)
                // Small stop-loss
                gain < -0.05 -> toSell.add(holding to holding.amount)
            }
        }

        if (toSell.isNotEmpty()) sell = TradingAlgorithm.Output.Sell(toSell)

        // Update history
        run {
            m_MovingWindow.pollFirst()
            m_MovingWindow.addLast(currentPrice)
        }

        return TradingAlgorithm.Output(buy, sell)
    }

    //===========================================================//
    //===========================================================//
    // Constructor(s)

    constructor(history: List<SecurityHistory>) {
        require(m_MWSize == history.size) { "Init" }

        val h1 = history.map { it.closingPrice }

        m_MovingWindow.addAll(h1)

        check(m_MovingWindow.size == m_MWSize) { "Init 2" }
    }

    //===========================================================//

    init {
        m_MovingWindow = ArrayDeque()
    }
}