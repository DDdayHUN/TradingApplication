package domain.algorithm

import data.repository.HistoricalMarketDataProvider
import domain.assets.security.SecurityHistory
import domain.assets.security.SecurityHolding
import domain.assets.security.SecurityIdentifier
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
            val a1 = async { HistoricalMarketDataProvider.loadFromFile(securityIdentifier, from, to).toMutableList() }
            a1.await()
        }
        val retTradingAlgorithm = when (type) {
            is Type.TACPP46 -> {
                val initNum = 42
                val init = retHistory.subList(0, initNum).toList()
                retHistory.subList(0, initNum).clear()
                TACPP46(init)
            }
            is Type.ALGDES2 -> {
                val initNum = 20
                val init = retHistory.subList(0, initNum).toList()
                retHistory.subList(0, initNum).clear()
                ALGDES2(init)
            }
            is Type.ALGDES3 -> {
                val initNum = 15
                val init = retHistory.subList(0, initNum).toList()
                retHistory.subList(0, initNum).clear()
                ALGDES3(init)
            }
            is Type.ALGDES31 -> {
                val initNum = 20
                val init = retHistory.subList(0, initNum).toList()
                retHistory.subList(0, initNum).clear()
                ALGDES31(init)
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
            val a1 = async { HistoricalMarketDataProvider.loadFromFile(securityIdentifier, Instant.DISTANT_PAST, Instant.DISTANT_FUTURE) }
            a1.await()
        }
        return when (type) {
            is Type.TACPP46 -> {
                val init = history.takeLast(42)
                TACPP46(init)
            }
            is Type.ALGDES2 -> {
                val init = history.takeLast(20)
                ALGDES2(init)
            }
            is Type.ALGDES3 -> {
                val init = history.takeLast(15)
                ALGDES3(init)
            }
            is Type.ALGDES31 -> {
                val init = history.takeLast(20)
                ALGDES31(init)
            }
        }
    }

    //===========================================================//
    //===========================================================//
    // Helper Class(es)

    sealed interface Type {
        data object TACPP46 : Type
        data object ALGDES2 : Type
        data object ALGDES3 : Type
        data object ALGDES31 : Type

        companion object {
            val entries: List<Type> = listOf(
                TACPP46,
                ALGDES2,
                ALGDES3,
                ALGDES31
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