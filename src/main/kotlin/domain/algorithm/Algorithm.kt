package domain.algorithm

import data.SerializationManager
import domain.stock.History
import domain.stock.Holding
import java.io.File

//===========================================================//
/**
 * Abstract base class for all trading algorithms.
 * 
 *  Defines the required interface and provides factory methods for initializing algorithms in different modes.
 */
//===========================================================//

abstract class Algorithm {
    //===========================================================//
    //===========================================================//
    // Public Method(es)
    /**
     * Executes the algorithm based on current holdings and market conditions.
     *
     * @param holdings      - List of currently owned assets.
     * @param allocatedCapital - Amount of capital allocated for trading.
     * @param currentPrice  - Current market price of the asset.
     * @return              AlgorithmOutput containing the decision/results.
     */
    abstract fun run(holdings: List<Holding>, allocatedCapital: Double, currentPrice: Double): Output

    //===========================================================//
    /**
     * Updates the internal state of the algorithm.
     *
     * @param history - Historical stock data to be incorporated.
     */
    abstract fun updateHistory(history: History)

    companion object {
        //===========================================================//
        /**
         * Initializes an algorithm instance configured for backtesting.
         *
         * @param type     - Type of algorithm to initialize.
         * @param stockName - Stock identifier/name.
         * @param from     - Start date (inclusive).
         * @param to       - End date (inclusive).
         * @return         Pair containing the list of history that was not used up for initialisation and the algorithm instance.
         */
        fun initForBackTest(type: Type, stockName: String, from: Int, to: Int): Pair<List<History>, Algorithm> {
            return initialiser(type, Algorithm.Init.BACKTEST, stockName, from, to)
        }

        //===========================================================//
        /**
         * Initializes an algorithm instance configured for live trading.
         *
         * @param type     - Type of algorithm to initialize.
         * @param stockName - Stock identifier/name.
         * @return         Initialized algorithm instance.
         */
        fun initForTrading(type: Algorithm.Type, stockName: String): Algorithm {
            return initialiser(type, Init.TRADING, stockName, Int.MIN_VALUE, Int.MAX_VALUE).second
        }

        //===========================================================//
        //===========================================================//
        // Private Method(es)
        /**
         * Core initialization method used by both trading and backtesting setups.
         *
         * @param type     - Algorithm type.
         * @param init     - Initialization mode.
         * @param stockName - Stock identifier/name.
         * @param from     - Start date (inclusive).
         * @param to       - End date (inclusive).
         * @return         Pair of history data and initialized algorithm.
         */
        private fun initialiser(type: Type, init: Init, stockName: String, from: Int, to: Int): Pair<List<History>, Algorithm> {
            val retHistory: List<History> = historyInitialiser(stockName, from, to)
            val retAlgorithm: Algorithm = when (type) {
                Type.TACPP46 -> TACPP46(init, retHistory.toMutableList())
            }
            return Pair(retHistory, retAlgorithm)
        }

        //===========================================================//
        /**
         * Loads historical data from files based on the given parameters.
         *
         * @param stockName - Stock identifier/name.
         * @param from     - Start date (inclusive).
         * @param to       - End date (inclusive).
         * @return         List of historical data entries.
         */
        private fun historyInitialiser(stockName: String, from: Int, to: Int): List<History> {
            val backtestFiles = File("src/main/resources/backtest/us/").listFiles() ?: error("Backtest directory is missing or not a directory")
            val proxy: MutableList<Pair<File, Int>> = ArrayList()

            for (file in backtestFiles) {
                val nameWithoutExtension = file.nameWithoutExtension
                val date = Integer.parseInt(nameWithoutExtension.substring(nameWithoutExtension.length - 2))
                val nameWithoutExtensionAndDate = nameWithoutExtension.substring(0, nameWithoutExtension.length - 2)

                if (date in from..to && nameWithoutExtensionAndDate == stockName) proxy.add(Pair(file, date))
            }


            proxy.sortWith(Comparator.comparingInt { h -> h.second }) // Sort chronologically by the date
            // If 'from' and/or 'to' are MIN and/or MAX then we've loaded all, so we don't throw.
            require(!(from != Integer.MIN_VALUE && to != Integer.MAX_VALUE && proxy.size != (to - from + 1))) { "From-To" }

            val ret: MutableList<History> = ArrayList()
            for (item in proxy) {
                val serData = SerializationManager.loadFromFile(item.first)
                for (item2 in serData.stockHistory.entries) {
                    ret.addAll(item2.value)
                }
            }

            return ret
        }
    }

    //===========================================================//
    //===========================================================//
    // Enum(s)

    /**
     * Supported algorithm types.
     */
    enum class Type {
        TACPP46,
    }

    //===========================================================//
    /**
     * Initialization modes for the algorithm.
     */
    internal enum class Init {
        BACKTEST,
        TRADING
    }

    //===========================================================//
    //===========================================================//
    // Helper class(es)

    data class Output(
        val buy: Buy?,
        val sell: Sell?
    ) {
        data class Buy(val amount: Long)
        data class Sell(val batches: List<Pair<Holding, Long>>)
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