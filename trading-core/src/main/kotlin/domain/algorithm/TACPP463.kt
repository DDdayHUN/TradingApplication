package domain.algorithm

import domain.market.security.SecurityHistory
import domain.market.security.SecurityHolding
import domain.utils.Math.rsi
import domain.utils.Math.stdDev
import java.util.*

//===========================================================//
/**
 * Stable baseline version of TACPP463.
 *
 * Uses:
 * - 39-period EMA
 * - 39-period volatility
 * - 14-period RSI
 * - one position at a time
 * - rebound confirmation
 * - volatility-based trailing stop
 * - volatility-based stop loss
 */
//===========================================================//

internal class TACPP463 : ITradingAlgorithm {

    //===========================================================//
    // Private Field(s)

    private val m_EmaWindow = 39
    private val m_RsiWindow = 14

    private val m_PositionSize = 0.20

    private val m_EmaHistory: Deque<Double>
    private val m_PriceHistory: Deque<Double>

    private val m_TrailingHigh: MutableMap<UUID, Double>
    private val m_MarkedForSelling: MutableMap<UUID, Int>

    private val m_LastInputArr: Deque<Double>

    //===========================================================//
    // Public Method(es)

    override fun run(
        holdings: Set<SecurityHolding>,
        allocatedCapital: Double,
        currentPrice: Double
    ): TradingAlgorithm.Output {

        var buy: TradingAlgorithm.Output.Buy? = null
        var sell: TradingAlgorithm.Output.Sell? = null

        //=======================================================//
        // Indicators

        val prices = ArrayList(m_PriceHistory)

        val std = prices.stdDev()

        val rsi =
            prices
                .takeLast(m_RsiWindow)
                .rsi()

        val ema =
            requireNotNull(m_EmaHistory.peekLast()) {
                "EMA history is empty"
            }

        val lowerBand =
            ema * (1.0 - 1.5 * std)

        //=======================================================//
        // Risk

        val trailingActivation = Math.clamp(
            std * 2.0,
            0.004,
            0.012
        )

        val trailingDistance = Math.clamp(
            std * 1.25,
            0.0025,
            0.008
        )

        val stopLoss = Math.clamp(
            std * 2.5,
            0.006,
            0.020
        )

        //=======================================================//
        // Buy

        /*
         * Only search for a new entry when we do not
         * already own a position.
         */
        if (
            holdings.isEmpty() &&
            rsi <= 30.0 &&
            currentPrice <= lowerBand
        ) {

            if (m_LastInputArr.isEmpty()) {

                // First oversold price
                m_LastInputArr.addLast(currentPrice)

            } else if (
                ArrayList(m_LastInputArr).average() <= currentPrice
            ) {

                /*
                 * Price stopped falling and moved back above
                 * the average of the recent oversold prices.
                 */
                val amount =
                    (
                            allocatedCapital *
                                    m_PositionSize /
                                    currentPrice
                            ).toInt()

                if (amount > 0) {
                    buy =
                        TradingAlgorithm.Output.Buy(amount)

                    m_LastInputArr.clear()
                }

            } else {

                // Price is still falling
                m_LastInputArr.addLast(currentPrice)

                if (m_LastInputArr.size > 5) {
                    m_LastInputArr.pollFirst()
                }
            }

        } else {
            m_LastInputArr.clear()
        }

        //=======================================================//
        // Sell

        val toBeSold:
                MutableSet<Pair<SecurityHolding, Int>> =
            HashSet()

        //=======================================================//
        // Trailing Profit

        for (item in holdings) {

            var isMarked =
                m_MarkedForSelling.containsKey(item.id)

            /*
             * Only activate the trailing mechanism after
             * the position has moved sufficiently into profit.
             */
            if (
                !isMarked &&
                currentPrice >
                item.purchasePrice *
                (1.0 + trailingActivation)
            ) {

                m_MarkedForSelling[item.id] =
                    item.amount

                m_TrailingHigh[item.id] =
                    currentPrice

                isMarked = true
            }

            if (isMarked) {

                var high =
                    m_TrailingHigh.getOrDefault(
                        item.id,
                        currentPrice
                    )

                // Update highest price since activation
                if (currentPrice > high) {
                    high = currentPrice

                    m_TrailingHigh[item.id] =
                        high
                }

                // Sell after pullback from the high
                if (
                    currentPrice <
                    high * (1.0 - trailingDistance)
                ) {

                    toBeSold.add(
                        Pair(item, item.amount)
                    )

                    m_MarkedForSelling.remove(item.id)
                    m_TrailingHigh.remove(item.id)
                }
            }
        }

        //=======================================================//
        // Stop Loss

        for (item in holdings) {

            if (
                currentPrice <
                item.purchasePrice *
                (1.0 - stopLoss)
            ) {

                toBeSold.add(
                    Pair(item, item.amount)
                )

                m_MarkedForSelling.remove(item.id)
                m_TrailingHigh.remove(item.id)
            }
        }

        //=======================================================//
        // Update Indicator State

        /*
         * We intentionally update AFTER making the decision.
         *
         * Therefore the currentPrice is evaluated against
         * indicators calculated from previous observations.
         */

        // Raw price history
        m_PriceHistory.pollFirst()
        m_PriceHistory.addLast(currentPrice)

        // EMA
        run {
            val alpha =
                2.0 / (m_EmaWindow + 1.0)

            val previousEma =
                requireNotNull(m_EmaHistory.peekLast()) {
                    "EMA history is empty"
                }

            val newEma =
                alpha * currentPrice +
                        (1.0 - alpha) * previousEma

            m_EmaHistory.pollFirst()
            m_EmaHistory.addLast(newEma)
        }

        //=======================================================//

        if (toBeSold.isNotEmpty()) {
            sell =
                TradingAlgorithm.Output.Sell(
                    toBeSold
                )
        }

        return TradingAlgorithm.Output(
            buy,
            sell
        )
    }

    //===========================================================//
    // Constructor(s)

    constructor(history: List<SecurityHistory>) {

        /*
         * 39 bars are used to seed the EMA.
         * Another 39 bars warm the EMA up.
         *
         * Total initialization:
         *
         * 39 + 39 = 78 bars
         */

        require(
            history.size >= 2 * m_EmaWindow
        ) {
            "Need at least ${2 * m_EmaWindow} initialization bars"
        }

        val q0 =
            history
                .subList(
                    0,
                    m_EmaWindow
                )
                .map {
                    it.closingPrice
                }

        val q1 =
            history
                .subList(
                    m_EmaWindow,
                    2 * m_EmaWindow
                )
                .map {
                    it.closingPrice
                }

        //=======================================================//
        // EMA initialization

        val alpha =
            2.0 / (m_EmaWindow + 1.0)

        var ema =
            q0.average()

        for (price in q1) {

            ema =
                alpha * price +
                        (1.0 - alpha) * ema

            m_EmaHistory.addLast(ema)
        }

        //=======================================================//
        // Raw price initialization

        for (price in q1) {
            m_PriceHistory.addLast(price)
        }

        //=======================================================//

        check(
            m_EmaHistory.size == m_EmaWindow
        ) {
            "EMA initialization failed"
        }

        check(
            m_PriceHistory.size == m_EmaWindow
        ) {
            "Price history initialization failed"
        }
    }

    //===========================================================//

    init {
        m_EmaHistory = ArrayDeque()
        m_PriceHistory = ArrayDeque()

        m_TrailingHigh = HashMap()
        m_MarkedForSelling = HashMap()

        m_LastInputArr = ArrayDeque()
    }
}