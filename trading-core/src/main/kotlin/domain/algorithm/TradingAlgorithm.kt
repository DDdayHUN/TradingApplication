package domain.algorithm

import com.google.gson.annotations.JsonAdapter
import domain.adapter.AlgorithmAdapter
import domain.market.security.SecurityHistory
import domain.market.security.SecurityHolding
import kotlin.collections.filter
import kotlin.time.Instant

//===========================================================//
/**
 * Sealed interface for all trading algorithms.
 * Defines the required interface and provides factory methods for initializing trading algorithms.
 */
//===========================================================//

@JsonAdapter(AlgorithmAdapter::class)
sealed interface TradingAlgorithm {
    //===========================================================//
    //===========================================================//
    // Public Method(es)

    /**
     * Executes the algorithm based on current holdings and market conditions.
     *
     * @param holdings the set of currently owned market assets.
     * @param allocatedCapital the amount of capital allocated for trading.
     * @param currentPrice the current market price of the asset.
     * @return contains the decision/results.
     */
    fun run(holdings: Set<SecurityHolding>, allocatedCapital: Double, currentPrice: Double): Output

    //===========================================================//
    //===========================================================//
    // Object - No override is needed after this line

    companion object {
        //===========================================================//
        //===========================================================//
        // Public Method(es)

        /**
         * Creates and initializes an algorithm instance configured for backtesting.
         *
         * @param type the type of algorithm to initialize.
         * @param history a list of by date sorted SecurityHistory on which we will initialize the algorithm.
         * @param from the start date (inclusive).
         * @param to the end date (inclusive).
         * @return a pair containing the list of history that was not used up for initialization and the algorithm instance.
         */
        fun create(type: Type, history: List<SecurityHistory>, from: Instant, to: Instant): Result<Pair<List<SecurityHistory>, TradingAlgorithm>> {
            return runCatching {
                val filteredHistory = history
                    .filter { it.timestamp in from..to }
                    .sortedBy { it.timestamp }

                val backtest = forBackTest(type, filteredHistory)
                val algorithm = createAlgorithm(type, backtest.first)

                Pair(backtest.second, algorithm)
            }
        }

        //===========================================================//
        /**
         * Creates and initializes an algorithm instance configured for trading.
         *
         * @param type the type of algorithm to initialize.
         * @param history a list of by date sorted SecurityHistory on which we will initialize the algorithm.
         * @return the configured algorithm instance.
         */
        fun create(type: Type, history: List<SecurityHistory>): Result<TradingAlgorithm> {
            return runCatching {
                val filteredHistory = history
                    .sortedBy { it.timestamp }

                val trading = forTrading(type, filteredHistory)
                val algorithm = createAlgorithm( type, trading)
                algorithm
            }
        }

        //===========================================================//
        //===========================================================//
        // Private Method(es)

        /**
         * Splits the historical data for backtesting into initial and remaining subsets.
         *
         * @param type the type of the algorithm for [Type.initSize].
         * @param history the full historical data of the given asset.
         * @return a pair containing the for initialization history as first, and the remaining history as second.
         */
        private fun forBackTest(type: Type, history: List<SecurityHistory>): Pair<List<SecurityHistory>, List<SecurityHistory>> {
            check(history.size >= type.initSize) { "Init" }

            val init = history.subList(0, type.initSize).toList()
            val remainder = history.drop(type.initSize)

            return Pair(init, remainder)
        }

        //===========================================================//
        /**
         * Prepares market history for live trading by retaining only the most recent data
         * required by the algorithm strategy.
         *
         *  @param type the type of the algorithm for [Type.initSize].
         *  @param history the history with of the given asset.
         *  @return the history .
         */
        private fun forTrading(type: Type, history: List<SecurityHistory>): List<SecurityHistory> {
            check(history.size >= type.initSize) { "Init" }

            return history.takeLast(type.initSize)
        }

        //===========================================================//
        /**
         * Factory function that instantiates a trading algorithm.
         *
         *  @param type the type of the algorithm to be instantiated.
         *  @param history the history with which we initialize the algorithm.
         *  @return the instantiated algorithm.
         */
        private fun createAlgorithm(type: Type, history: List<SecurityHistory>): TradingAlgorithm {
            check(history.size == type.initSize) { "Size" }

            return when (type) {
                is Type.TACPP46 -> TACPP46(history)
                is Type.ALGDES2 -> ALGDES2(history)
                is Type.ALGDES3 -> ALGDES3(history)
                is Type.ALGDES31 -> ALGDES31(history)
                is Type.ALGDES4 -> ALGDES4(history)
                is Type.BUYANDHOLD -> BUYANDHOLD()
                is Type.TACPP462 ->TACPP462(history)
                is Type.TACPP45 -> TACPP45(history)
            }
        }

        //===========================================================//

        val REGISTRY = listOf(
            RegistryEntry("TACPP46",       Type.TACPP46,      TACPP46::class.java),
            RegistryEntry("ALGDES2",       Type.ALGDES2,      ALGDES2::class.java),
            RegistryEntry("ALGDES3",       Type.ALGDES3,      ALGDES3::class.java),
            RegistryEntry("ALGDES31",      Type.ALGDES31,     ALGDES31::class.java),
            RegistryEntry("ALGDES4",       Type.ALGDES4,      ALGDES4::class.java),
            RegistryEntry("BUYANDHOLD",    Type.BUYANDHOLD,   BUYANDHOLD::class.java),
            RegistryEntry("TACPP462",      Type.TACPP462,     TACPP462::class.java),
            RegistryEntry("TACPP45",       Type.TACPP45,      TACPP45::class.java),
        )

        private val sp_TagToType  = REGISTRY.associate { it.tag to it.type }
        //private val sp_TypeToTag  = REGISTRY.associate { it.type to it.tag }
        private val sp_TagToClass = REGISTRY.associate { it.tag to it.clazz }
        private val sp_ClassToTag = REGISTRY.associate { it.clazz to it.tag }

        //===========================================================//

        fun typeFromTag(tag: String): Result<Type> {
            return runCatching {
                sp_TagToType[tag]
                    ?: throw IllegalArgumentException("Unknown Trading algorithm type with tag: $tag")
            }
        }

        //===========================================================//

        fun classFromTag(tag: String): Result<Class<out TradingAlgorithm>> {
            return runCatching {
                sp_TagToClass[tag]
                    ?: throw IllegalArgumentException("Unknown Trading algorithm type with tag: $tag")
            }
        }

        //===========================================================//

        fun typeTagOf(algorithm: TradingAlgorithm): Result<String> {
            return runCatching {
                sp_ClassToTag[algorithm::class.java]
                    ?: throw IllegalArgumentException("Unknown Trading algorithm type: $algorithm")
            }
        }
    }

    //===========================================================//
    //===========================================================//
    // Helper Class(es)

    sealed interface Type {
        val initSize: Int

        data object TACPP46 : Type { override val initSize = 42 }
        data object ALGDES2 : Type { override val initSize = 20 }
        data object ALGDES3 : Type { override val initSize = 15 }
        data object ALGDES31 : Type { override val initSize = 20 }
        data object ALGDES4 : Type { override val initSize = 7 }
        data object BUYANDHOLD : Type { override val initSize = 0 }
        data object TACPP462 : Type { override val initSize = 42 }
        data object TACPP45 : Type { override val initSize = 56 }
    }

    //===========================================================//

    data class Output(
        val buy: Buy?,
        val sell: Sell?
    ) {
        data class Buy(val amount: Int) {
            init {
                require(amount > 0) { "Amount must be greater than 0" }
            }
        }
        data class Sell(val batches: Set<Pair<SecurityHolding, Int>>) {
            init {
                require(batches.isNotEmpty()) { "Batches must be non empty" }
            }
        }
    }

    //===========================================================//

    data class RegistryEntry(
        val tag: String,
        val type: Type,
        val clazz: Class<out TradingAlgorithm>
    )
}