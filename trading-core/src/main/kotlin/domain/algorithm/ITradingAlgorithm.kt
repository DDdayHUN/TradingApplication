package domain.algorithm

import com.google.gson.annotations.JsonAdapter
import domain.adapter.AlgorithmAdapter
import domain.algorithm.TradingAlgorithm.Output
import domain.market.security.SecurityHolding

@JsonAdapter(AlgorithmAdapter::class)
sealed interface ITradingAlgorithm {
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

    companion object {
        fun typeTagOf(algorithm: ITradingAlgorithm): String {
            return when (algorithm){
                is TACPP46 -> "TACPP46"
                is ALGDES2 -> "ALGDES2"
                is ALGDES3 -> "ALGDES3"
                is ALGDES31 -> "ALGDES31"
                is ALGDES4 -> "ALGDES4"
                is BUYANDHOLD -> "BUYANDHOLD"
                is TACPP462 -> "TACPP462"
            }
        }
    }
}