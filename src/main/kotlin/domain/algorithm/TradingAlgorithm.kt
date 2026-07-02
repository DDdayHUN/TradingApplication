package domain.algorithm

import data.SerializationManager
import domain.assets.security.SecurityHistory
import domain.assets.security.SecurityHolding
import java.io.File
import kotlin.time.Instant

//===========================================================//
/**
 * Abstract base class for all trading algorithms.
 * Defines the required interface and provides factory methods for initializing algorithms in different modes.
 */
//===========================================================//

abstract class TradingAlgorithm {
    //===========================================================//
    //===========================================================//
    // Public Method(es)

    /**
     * Executes the algorithm based on current holdings and market conditions.
     *
     * @param holdings List of currently owned assets.
     * @param allocatedCapital Amount of capital allocated for trading.
     * @param currentPrice Current market price of the asset.
     * @return AlgorithmOutput containing the decision/results.
     */
    abstract fun run(holdings: List<SecurityHolding>, allocatedCapital: Double, currentPrice: Double): Output

    //===========================================================//
    /**
     * Updates the internal state of the algorithm.
     *
     * @param history - Historical stock data to be incorporated.
     */
    abstract fun updateState(currentPrice: Double, history: SecurityHistory)

    companion object {
        //===========================================================//
        /**
         * Initializes an algorithm instance configured for backtesting.
         *
         * @param type Type of algorithm to initialize.
         * @param stockName Stock identifier/name.
         * @param from Start date (inclusive).
         * @param to End date (inclusive).
         * @return Pair containing the list of history that was not used up for initialization and the algorithm instance.
         */
        @Deprecated("This will get phased out in favor of the other initialiser")
        fun create(type: Type, stockName: String, from: Int, to: Int): Pair<List<SecurityHistory>, TradingAlgorithm> {
            return initialiser(type, Init.BACKTEST, stockName, from, to)
        }

        //===========================================================//
        /**
         * Creates and initializes an algorithm instance configured for backtesting.
         *
         * @param type Type of algorithm to initialize.
         * @param stockName Stock identifier/name.
         * @param from Start date (inclusive).
         * @param to End date (inclusive).
         * @return Pair containing the list of history that was not used up for initialization and the algorithm instance.
         */
        fun create(type: Type, stockName: String, from: Instant, to: Instant): Pair<List<SecurityHistory>, TradingAlgorithm> {
            return initialiser(type, Init.BACKTEST, stockName, from, to)
        }

        //===========================================================//
        /**
         * Creates and initializes an algorithm instance configured for trading.
         *
         * @param type Type of algorithm to initialize.
         * @param stockName Stock identifier/name.
         * @return the configured algorithm instance.
         */
        fun create(type: Type, stockName: String): Pair<List<SecurityHistory>, TradingAlgorithm> {
            return initialiser(type, Init.BACKTEST, stockName, Instant.DISTANT_PAST, Instant.DISTANT_FUTURE)
        }

        //===========================================================//
        /**
         * Initializes an algorithm instance configured for backtesting.
         *
         * @param type Type of algorithm to initialize.
         * @param stockName Stock identifier/name.
         * @param from Start date (inclusive).
         * @param to End date (inclusive).
         * @return Pair containing the list of history that was not used up for initialization and the algorithm instance.
         */

        //===========================================================//
        //===========================================================//
        // Private Method(es)

        /**
         * Core initialization method used by both trading and backtesting setups.
         *
         * @param type Algorithm type.
         * @param init Initialization mode.
         * @param stockName Stock name.
         * @param from Start date (inclusive).
         * @param to End date (inclusive).
         * @return Pair of history data and initialized algorithm.
         */
        @Deprecated("This will get phased out in favor of the other initialiser")
        private fun initialiser(type: Type, init: Init, stockName: String, from: Int, to: Int): Pair<List<SecurityHistory>, TradingAlgorithm> {
            val retHistory = SerializationManager.loadHistoryForAlgorithms(stockName, from, to)
            val retTradingAlgorithm: TradingAlgorithm = when (type) {
                is Type.TACPP46 -> TACPP46(init, retHistory)
            }
            return Pair(retHistory, retTradingAlgorithm)
        }

        //===========================================================//
        /**
         * Core initialization method used by both trading and backtesting setups.
         *
         * @param type Algorithm type.
         * @param init Initialization mode.
         * @param stockName Stock name.
         * @param from Start date (inclusive).
         * @param to End date (inclusive).
         * @return Pair of history data and initialized algorithm.
         */
        private fun initialiser(type: Type, init: Init, stockName: String, from: Instant, to: Instant): Pair<List<SecurityHistory>, TradingAlgorithm> {
            val retHistory = SerializationManager.loadBackTestData(stockName, from, to)
            val retTradingAlgorithm: TradingAlgorithm = when (type) {
                is Type.TACPP46 -> TACPP46(init, retHistory)
            }
            return Pair(retHistory, retTradingAlgorithm)
        }
    }

    //===========================================================//
    //===========================================================//
    // Enum(s)

    /**
     * Supported algorithm types.
     */
    sealed interface Type {
        data object TACPP46 : Type
    }

    //===========================================================//
    /**
     * Initialization modes for the algorithm.
     */
    sealed interface Init {
        data object BACKTEST: Init
        data object TRADING: Init
    }

    //===========================================================//
    //===========================================================//
    // Helper Class(es)

    data class Output(
        val buy: Buy?,
        val sell: Sell?
    ) {
        data class Buy(val amount: Long)
        data class Sell(val batches: List<Pair<SecurityHolding, Long>>)
    }

    //===========================================================//
    //===========================================================//
    // Constructor(s)

    /**
     * Package constructor to enforce initialization mode handling
     * in subclasses.
     *
     * @param init - Initialization mode.
     */
    internal constructor(init: Init)
}