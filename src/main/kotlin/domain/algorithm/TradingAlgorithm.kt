package domain.algorithm

import data.repository.historical_data.HistoricalMarketDataProvider
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
    fun create(type: Type, securityIdentifier: SecurityIdentifier, from: Instant, to: Instant): Pair<List<SecurityHistory>, ITradingAlgorithm> {
        return initForBackTest(type, securityIdentifier, from, to)
    }

    //===========================================================//
    /**
     * Creates and initializes an algorithm instance configured for trading.
     *
     * @param type the type of algorithm to initialize.
     * @param securityIdentifier the identifier identifies a security.
     * @return the configured algorithm instance.
     */
    fun create(type: Type, securityIdentifier: SecurityIdentifier): ITradingAlgorithm {
        return initForTrading(type, securityIdentifier)
    }

    //===========================================================//
    //===========================================================//
    // Private Method(es)

    /**
     * Core initialization method used by backtesting setups.
     *
     * @param type the type of algorithm to initialize.
     * @param securityIdentifier the identifier identifies a security.
     * @param from the start date (inclusive).
     * @param to the end date (inclusive).
     * @return a pair that consists of history data that has not been used up in the initialization process and of an initialized algorithm.
     */
    private fun initForBackTest(type: Type, securityIdentifier: SecurityIdentifier, from: Instant, to: Instant): Pair<List<SecurityHistory>, ITradingAlgorithm> {
        val retHistory = runBlocking {
            val a1 = async { HistoricalMarketDataProvider.getBySecurityIdentifier(securityIdentifier, from, to).getOrThrow().toMutableList() }
            a1.await()
        }
        val retTradingAlgorithm = when (type) {
            is Type.TACPP46 -> {
                val init = retHistory.subList(0, type.initSize).toList()
                retHistory.subList(0, type.initSize).clear()
                TACPP46(init)
            }
            is Type.ALGDES2 -> {
                val init = retHistory.subList(0, type.initSize).toList()
                retHistory.subList(0, type.initSize).clear()
                ALGDES2(init)
            }
            is Type.ALGDES3 -> {
                val init = retHistory.subList(0, type.initSize).toList()
                retHistory.subList(0, type.initSize).clear()
                ALGDES3(init)
            }
            is Type.ALGDES31 -> {
                val init = retHistory.subList(0, type.initSize).toList()
                retHistory.subList(0, type.initSize).clear()
                ALGDES31(init)
            }
            is Type.ALGDES4 -> {
                val init = retHistory.subList(0, type.initSize).toList()
                retHistory.subList(0, type.initSize).clear()
                ALGDES4(init)
            }
        }
        return Pair(retHistory, retTradingAlgorithm)
    }

    //===========================================================//
    /**
     * Core initialization method used by trading setups.
     *
     * @param type the type of algorithm to initialize.
     * @param securityIdentifier the identifier identifies a security.
     * @return an initialized algorithm for trading.
     */
    private fun initForTrading(type: Type, securityIdentifier: SecurityIdentifier): ITradingAlgorithm {
        val history = runBlocking {
            val a1 = async { HistoricalMarketDataProvider.getBySecurityIdentifier(securityIdentifier, Instant.DISTANT_PAST, Instant.DISTANT_FUTURE) }
            a1.await().getOrThrow()
        }
        return when (type) {
            is Type.TACPP46 -> {
                TACPP46(history.takeLast(type.initSize))
            }
            is Type.ALGDES2 -> {
                ALGDES2(history.takeLast(type.initSize))
            }
            is Type.ALGDES3 -> {
                ALGDES3(history.takeLast(type.initSize))
            }
            is Type.ALGDES31 -> {
                ALGDES31(history.takeLast(type.initSize))
            }
            is Type.ALGDES4 -> {
                ALGDES4(history.takeLast(type.initSize))
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

        val initSize: Int

        companion object {
            val entries: List<Type> = listOf(
                TACPP46,
                ALGDES2,
                ALGDES3,
                ALGDES31,
                ALGDES4
            )
        }
    }

    //===========================================================//

    data class Output(
        val buy: Buy?,
        val sell: Sell?
    ) {
        data class Buy(val amount: Int)
        data class Sell(val batches: List<Pair<SecurityHolding, Int>>)
    }
}