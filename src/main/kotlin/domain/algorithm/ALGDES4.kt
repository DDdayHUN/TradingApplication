package domain.algorithm

import domain.market.security.SecurityHistory
import domain.market.security.SecurityHolding
import java.util.ArrayDeque
import java.util.Deque
import kotlin.math.abs

internal class ALGDES4 : ITradingAlgorithm {
    //===========================================================//
    //===========================================================//
    // Private Field(s)

    private val m_MWSize = 7
    private val m_MovingWindow: Deque<Double>

    private var m_BuySignalCount = 0
    private var m_MaxConfidenceInStreak = 0.0
    private var m_LowestPriceInStreak = 0.0

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

        val deviation = (lowerBand - currentPrice) / mean
        val volatilityFactor = 1.0 / (std + 1e-6)
        val rawScore = deviation * volatilityFactor
        val currentConfidence = Math.clamp(rawScore, 0.05, 0.25)

        //-------------------------------------------------------
        // Buy

        if (currentPrice < lowerBand) {
            m_BuySignalCount++

            // Save the best (deepest) confidence score to use when we finally buy
            if (currentConfidence > m_MaxConfidenceInStreak) m_MaxConfidenceInStreak = currentConfidence

            // Keep track of the previous price to spot the exact moment it ticks up
            m_LowestPriceInStreak =
                if (m_LowestPriceInStreak == 0.0) currentPrice
                else minOf(m_LowestPriceInStreak, currentPrice)

        } else {
            // Price is back above the lower band.
            // If the streak didn't break on the way up, handle any leftover signal here.
            if (m_BuySignalCount > 0) {
                val capitalToUse = allocatedCapital * m_MaxConfidenceInStreak
                val amount = (capitalToUse / currentPrice).toInt()
                if (amount > 0) buy = TradingAlgorithm.Output.Buy(amount)
            }

            // Reset everything
            m_BuySignalCount = 0
            m_MaxConfidenceInStreak = 0.0
            m_LowestPriceInStreak = 0.0
        }

        // Mid-streak reversal check: If we are in a deep dip and price starts bouncing up, BUY NOW
        if (m_BuySignalCount > 1 && currentPrice > m_LowestPriceInStreak) {
            val capitalToUse = allocatedCapital * m_MaxConfidenceInStreak
            val amount = (capitalToUse / currentPrice).toInt()
            if (amount > 0) buy = TradingAlgorithm.Output.Buy(amount)

            // Reset streak immediately so we don't buy multiple times on the same dip
            run {
                m_BuySignalCount = 0
                m_MaxConfidenceInStreak = 0.0
                m_LowestPriceInStreak = 0.0
            }
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