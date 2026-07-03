package domain.algorithm

import domain.algorithm.TradingAlgorithm.Output
import domain.assets.security.SecurityHolding

sealed interface ITradingAlgorithm {
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
    fun run(holdings: List<SecurityHolding>, allocatedCapital: Double, currentPrice: Double): Output
}