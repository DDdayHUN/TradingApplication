package domain.algorithm

import domain.assets.security.SecurityHistory
import domain.assets.security.SecurityHolding
import java.util.ArrayDeque
import java.util.Deque

internal class ALGDES31 : ITradingAlgorithm {
    //===========================================================//
    //===========================================================//
    // Private Field(s)

    private val m_MWSize = 20
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
        val risk = Math.clamp(std * 100.0, 0.1, 0.3)

        val lowerBand = mean - std
        val upperBand = mean + std

        val deviation = (lowerBand - currentPrice) / mean
        val volatilityFactor = 1.0 / (std + 1e-6)
        val rawScore = deviation * volatilityFactor
        val confidence = Math.clamp(rawScore, 0.01, 0.1)
        val capitalToUse = allocatedCapital * confidence


        //-------------------------------------------------------
        // Buy

        if (currentPrice < lowerBand) {
            val amount = (capitalToUse / currentPrice).toInt()
            if (amount > 0) buy = TradingAlgorithm.Output.Buy(amount)
        }

        //-------------------------------------------------------
        // Sell

        val toSell = mutableListOf<Pair<SecurityHolding, Int>>()

        for (holding in holdings) {
            val gain = (currentPrice - holding.entryPrice) / holding.entryPrice

            when {
                // Price is high relative to average
                currentPrice > 4.0 * upperBand -> toSell.add(holding to holding.amount)
                // Small stop-loss
                gain < (-1).div(risk) -> toSell.add(holding to holding.amount)
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