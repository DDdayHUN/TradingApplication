package domain.algorithm

import domain.assets.security.SecurityHistory
import domain.assets.security.SecurityHolding
import java.util.ArrayDeque
import java.util.Deque

/**
 * AI Generalt ez is soooo, we will seee.
 * Nem is rossz, kellett egy sok tweaking, de nem is rossz.
 */
class ALGDES2 : ITradingAlgorithm {
    //===========================================================//
    //===========================================================//
    // Private Field(s)

    private val m_MWSize = 20 // 30-as ertek sem rossz.
    private val m_MovingWindow: Deque<Double>

    //===========================================================//
    //===========================================================//
    // Public Method(es)

    override fun run(holdings: List<SecurityHolding>, allocatedCapital: Double, currentPrice: Double): TradingAlgorithm.Output {
        var buy: TradingAlgorithm.Output.Buy? = null
        var sell: TradingAlgorithm.Output.Sell? = null

        val history = m_MovingWindow.toList()

        val mean = utils.Math.average(history)
        val std = utils.Math.stdDev(history)
        val risk = Math.clamp(std * 100.0, 0.05, 0.2)

        val lowerBand = mean - std * 2
        val upperBand = mean + std * 2

        //-------------------------------------------------------
        // Buy

        if (currentPrice < lowerBand) {
            val confidence = Math.clamp(1.div(risk), 0.05, 0.25)
            val amount = (allocatedCapital * confidence / currentPrice).toLong()

            if (amount > 0) buy = TradingAlgorithm.Output.Buy(amount)
        }

        //-------------------------------------------------------
        // Sell

        val toSell = mutableListOf<Pair<SecurityHolding, Long>>()

        for (holding in holdings) {
            val gain = (currentPrice - holding.entryPrice) / holding.entryPrice

            when {
                // Price is high relative to average
                currentPrice > 2 * upperBand * (1.0 - risk) -> toSell.add(holding to holding.amount)
                // Small stop-loss
                gain < -0.1 -> toSell.add(holding to holding.amount)
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