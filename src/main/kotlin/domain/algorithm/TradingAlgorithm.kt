package domain.algorithm

import data.SerializationManager
import domain.assets.security.SecurityHistory
import domain.assets.security.SecurityHolding
import domain.assets.security.SecurityIdentifier
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
     * @param holdings the list of currently owned assets.
     * @param allocatedCapital the amount of capital allocated for trading.
     * @param currentPrice the current market price of the asset.
     * @return contains the decision/results.
     */
    abstract fun run(holdings: List<SecurityHolding>, allocatedCapital: Double, currentPrice: Double): Output

    companion object {
        //===========================================================//
        /**
         * Creates and initializes an algorithm instance configured for backtesting.
         *
         * @param type the type of algorithm to initialize.
         * @param securityIdentifier the identifier identifies a security.
         * @param from the start date (inclusive).
         * @param to the end date (inclusive).
         * @return a pair containing the list of history that was not used up for initialization and the algorithm instance.
         */
        fun create(type: Type, securityIdentifier: SecurityIdentifier, from: Instant, to: Instant): Pair<List<SecurityHistory>, TradingAlgorithm> {
            return initialiser(type, Init.BACKTEST, securityIdentifier, from, to)
        }

        //===========================================================//
        /**
         * Creates and initializes an algorithm instance configured for trading.
         *
         * @param type the type of algorithm to initialize.
         * @param securityIdentifier the identifier identifies a security.
         * @return the configured algorithm instance.
         */
        fun create(type: Type, securityIdentifier: SecurityIdentifier): Pair<List<SecurityHistory>, TradingAlgorithm> {
            return initialiser(type, Init.BACKTEST, securityIdentifier, Instant.DISTANT_PAST, Instant.DISTANT_FUTURE)
        }

        //===========================================================//
        //===========================================================//
        // Private Method(es)

        /**
         * Core initialization method used by both trading and backtesting setups.
         *
         * @param type the type of algorithm to initialize.
         * @param init the initialization mode of the algorithm.
         * @param securityIdentifier the identifier identifies a security.
         * @param from the start date (inclusive).
         * @param to the end date (inclusive).
         * @return a pair that consists of history data that has not been used up in the initialization process and of an initialized algorithm.
         */
        private fun initialiser(type: Type, init: Init, securityIdentifier: SecurityIdentifier, from: Instant, to: Instant): Pair<List<SecurityHistory>, TradingAlgorithm> {
            val retHistory = SerializationManager.loadHistoricalDataFromFile(securityIdentifier, from, to)
            val retTradingAlgorithm: TradingAlgorithm = when (type) {
                is Type.TACPP46 -> TACPP46(init, retHistory)
            }
            return Pair(retHistory, retTradingAlgorithm)
        }
    }

    //===========================================================//
    //===========================================================//
    // Enum(s)

    sealed interface Type {
        data object TACPP46 : Type
    }

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
     * @param init the initialization mode of the algorithm.
     */
    internal constructor(init: Init)
}