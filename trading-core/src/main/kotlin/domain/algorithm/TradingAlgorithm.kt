package domain.algorithm

import domain.interfaces.IHistoricalMarketDataProvider
import domain.market.security.SecurityHistory
import domain.market.security.SecurityHolding
import domain.market.security.SecurityIdentifier
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import kotlin.time.Instant

//===========================================================//
/**
 * Abstract base class for all trading algorithms.
 * Defines the required interface and provides factory methods for initializing algorithms in different modes.
 */
//===========================================================//

object TradingAlgorithm {
    //===========================================================//
    //===========================================================//
    // Public Method(es)

    /**
     * Creates and initializes an algorithm instance configured for backtesting.
     *
     * @param type the type of algorithm to initialize.
     * @param securityIdentifier the identifier identifies a security.
     * @param from the start date (inclusive).
     * @param to the end date (inclusive).
     * @return a pair containing the list of history that was not used up for initialization and the algorithm instance.
     */
    fun create(provider: IHistoricalMarketDataProvider, type: Type, securityIdentifier: SecurityIdentifier, from: Instant, to: Instant): Pair<List<SecurityHistory>, ITradingAlgorithm> {
        val history = getHistory(provider, securityIdentifier, from, to)
        val backtest = forBackTest(type, history)
        val algorithm = create(provider, type, securityIdentifier, backtest.first)
        return Pair(backtest.second, algorithm)
    }

    //===========================================================//
    /**
     * Creates and initializes an algorithm instance configured for trading.
     *
     * @param type the type of algorithm to initialize.
     * @param securityIdentifier the identifier identifies a security.
     * @return the configured algorithm instance.
     */
    fun create(provider: IHistoricalMarketDataProvider, type: Type, securityIdentifier: SecurityIdentifier, history: List<SecurityHistory>? = null): ITradingAlgorithm {
        val history = getHistory(provider, securityIdentifier)
        val trading = forTrading(type, history)
        val algorithm = create(provider, type, securityIdentifier, trading)
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

    /**
     * Splits the historical data for backtesting into initial and remaining subsets.
     *
     * @param type the type of the algorithm for [Type.initSize].
     * @param history the full historical data of the given asset.
     * @return a pair containing the for initialization history as first, and the remaining history as second.
     */
    private fun forBackTest(type: Type, history: List<SecurityHistory>): Pair<List<SecurityHistory>, List<SecurityHistory>> {
        val init = history.subList(0, type.initSize).toList()
        val remainder = history.drop(type.initSize)

        return Pair(init, remainder)
    }

    /**
     * Prepares market history for live trading by retaining only the most recent data
     * required by the algorithm strategy.
     *
     *  @param type the type of the algorithm for [Type.initSize].
     *  @param history the history with of the given asset.
     *  @return the history .
     */
    private fun forTrading(type: Type, history: List<SecurityHistory>): List<SecurityHistory> {
        return history.takeLast(type.initSize)
    }

    /**
     * Factory function that instantiates a trading algorithm.
     *
     *  @param type the type of the algorithm to be instantiated.
     *  @param history the history with which we initialize the algorithm.
     *  @return the instantiated algorithm.
     */
    private fun createAlgorithm(type: Type, history: List<SecurityHistory>): ITradingAlgorithm {
        require(history.size == type.initSize) { "Size" }

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
        }
    }

    //===========================================================//
    //===========================================================//
    // Helper Class(es)

    sealed interface Type {
        data object TACPP46 : Type { override val initSize = 42 }
        data object ALGDES2 : Type { override val initSize = 20 }
        data object ALGDES3 : Type { override val initSize = 15 }
        data object ALGDES31 : Type { override val initSize = 20 }
        data object ALGDES4 : Type { override val initSize = 7 }
        data object BUYANDHOLD : Type { override val initSize = 0 }
        data object TACPP462 : Type { override val initSize = 42 }

        val initSize: Int

        companion object {
            val entries: List<Type> = listOf(
                TACPP46,
                ALGDES2,
                ALGDES3,
                ALGDES31,
                ALGDES4,
                BUYANDHOLD,
                TACPP462
            )
        }
    }

    //===========================================================//

    data class Output(
        val buy: Buy?,
        val sell: Sell?
    ) {
        data class Buy(val amount: Int)
        data class Sell(val batches: Set<Pair<SecurityHolding, Int>>)
    }
}