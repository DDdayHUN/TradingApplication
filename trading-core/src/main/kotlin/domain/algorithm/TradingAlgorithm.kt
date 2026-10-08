package domain.algorithm

import com.google.gson.annotations.JsonAdapter
import data.repository.historical_data.IHistoricalMarketDataProvider
import domain.adapter.AlgorithmAdapter
import domain.market.security.SecurityHistory
import domain.market.security.SecurityHolding
import domain.market.security.SecurityIdentifier
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import kotlin.collections.filter
import kotlin.time.Instant

//===========================================================//
/**
 * Abstract base class for all trading algorithms.
 * Defines the required interface and provides factory methods for initializing algorithms in different modes.
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
         * @param provider the provider which provides the historical market data.
         * @param type the type of algorithm to initialize.
         * @param securityIdentifier the identifier identifies a security.
         * @param from the start date (inclusive).
         * @param to the end date (inclusive).
         * @return a pair containing the list of history that was not used up for initialization and the algorithm instance.
         */
        fun create(provider: IHistoricalMarketDataProvider, type: Type, securityIdentifier: SecurityIdentifier, from: Instant, to: Instant): Pair<List<SecurityHistory>, TradingAlgorithm> {
            val history = getHistory(provider, securityIdentifier, from, to)
            val backtest = forBackTest(type, history)
            val algorithm = createAlgorithm(type, backtest.first)
            return Pair(backtest.second, algorithm)
        }

        //===========================================================//
        /**
         * Creates and initializes an algorithm instance configured for backtesting.
         *
         * @param history a list of by date sorted SecurityHistory on which we will initialize the algorithm.
         * @param type the type of algorithm to initialize.
         * @param from the start date (inclusive).
         * @param to the end date (inclusive).
         * @return a pair containing the list of history that was not used up for initialization and the algorithm instance.
         */
        // TODO : Maybe do a sort here on history just in case?
        fun create(history: List<SecurityHistory>, type: Type, from: Instant, to: Instant): Pair<List<SecurityHistory>, TradingAlgorithm> {
            val filteredHistory = history
                .filter { it.timestamp in from..to }
                .sortedBy { it.timestamp }

            val backtest = forBackTest(type, filteredHistory)
            val algorithm = createAlgorithm(type, backtest.first)
            return Pair(backtest.second, algorithm)
        }

        //===========================================================//
        /**
         * Creates and initializes an algorithm instance configured for trading.
         *
         * @param provider the provider which provides the historical market data.
         * @param type the type of algorithm to initialize.
         * @param securityIdentifier the identifier identifies a security.
         * @return the configured algorithm instance.
         */
        // TODO : Maybe make it so that we give a list instead?
        fun create(provider: IHistoricalMarketDataProvider, type: Type, securityIdentifier: SecurityIdentifier): TradingAlgorithm {
            val history = getHistory(provider, securityIdentifier)
            val trading = forTrading(type, history)
            val algorithm = createAlgorithm( type, trading)
            return algorithm
        }

        //===========================================================//
        //===========================================================//
        // Private Method(es)

        /**
         * Fetches market history for a given security between specified timestamps.
         *
         * @param provider the market data provider instance.
         * @param securityIdentifier the identifier of the security to query.
         * @param from the start instant of the historical range.
         * @param to the end instant of the historical range.
         * @return the list of historical market entries.
         */
        private fun getHistory(
            provider: IHistoricalMarketDataProvider,
            securityIdentifier: SecurityIdentifier,
            from: Instant = Instant.DISTANT_PAST,
            to: Instant = Instant.DISTANT_FUTURE
        ): List<SecurityHistory> {
            return runBlocking {
                async {
                    provider.getBySecurityIdentifier(securityIdentifier, from, to).getOrThrow()
                }.await()
            }
        }

        //===========================================================//
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
                is Type.TACPP46 -> {
                    TACPP46(history)
                }
                is Type.ALGDES2 -> {
                    ALGDES2(history)
                }
                is Type.ALGDES3 -> {
                    ALGDES3(history)
                }
                is Type.ALGDES31 -> {
                    ALGDES31(history)
                }
                is Type.ALGDES4 -> {
                    ALGDES4(history)
                }
                is Type.BUYANDHOLD -> {
                    BUYANDHOLD()
                }
                is Type.TACPP462 -> {
                    TACPP462(history)
                }
                is Type.TACPP45 -> {
                    TACPP45(history)
                }
            }
        }

        //===========================================================//

        val REGISTRY = listOf(
            AlgorithmRegistryEntry("TACPP46",       Type.TACPP46,      TACPP46::class.java),
            AlgorithmRegistryEntry("ALGDES2",       Type.ALGDES2,      ALGDES2::class.java),
            AlgorithmRegistryEntry("ALGDES3",       Type.ALGDES3,      ALGDES3::class.java),
            AlgorithmRegistryEntry("ALGDES31",      Type.ALGDES31,     ALGDES31::class.java),
            AlgorithmRegistryEntry("ALGDES4",       Type.ALGDES4,      ALGDES4::class.java),
            AlgorithmRegistryEntry("BUYANDHOLD",    Type.BUYANDHOLD,   BUYANDHOLD::class.java),
            AlgorithmRegistryEntry("TACPP462",      Type.TACPP462,     TACPP462::class.java),
            AlgorithmRegistryEntry("TACPP45",       Type.TACPP45,      TACPP45::class.java),
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

    data class AlgorithmRegistryEntry(
        val tag: String,
        val type: Type,
        val clazz: Class<out TradingAlgorithm>
    )
}