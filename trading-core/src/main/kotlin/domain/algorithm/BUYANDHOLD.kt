package domain.algorithm

import domain.market.security.SecurityHolding

//===========================================================//
/**
 * An implementation of [TradingAlgorithm].
 */
//===========================================================//

internal class BUYANDHOLD: ITradingAlgorithm {
    //===========================================================//
    //===========================================================//
    // Public Method(es)

    override fun run(holdings: Set<SecurityHolding>, allocatedCapital: Double, currentPrice: Double): TradingAlgorithm.Output {
        val amount = (allocatedCapital / currentPrice).toInt()
        if (amount <= 0) { return TradingAlgorithm.Output(null, null) }

        return TradingAlgorithm.Output(TradingAlgorithm.Output.Buy(amount), null)
    }
}