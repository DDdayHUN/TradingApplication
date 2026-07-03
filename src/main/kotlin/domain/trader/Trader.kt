package domain.trader

import domain.algorithm.ITradingAlgorithm
import domain.algorithm.TradingAlgorithm
import domain.assets.Quote
import domain.assets.security.SecurityHolding
import domain.assets.security.SecurityIdentifier
import domain.signal.TradingSignal

//===========================================================//
/**
 * Represents a virtual trader that is responsible for one security
 *
 * The trader owns allocated capital, currently held securities and activate
 * trading algorithm that is (will be) decided by the Algorithm Manager.
 * It only creates trading signals based on given quote
 *
 */
// ===========================================================//

class Trader {
    //===========================================================//
    //===========================================================//
    // Public Field(s)

    val securityIdentifier: SecurityIdentifier

    //===========================================================//
    //===========================================================//
    // Private Field(s)

    private var m_CurrentCapital: Double
    private val m_Holdings: MutableList<SecurityHolding>
    private var m_Algorithm: ITradingAlgorithm

    //===========================================================//
    //===========================================================//
    // Public Method(es)

    fun createSignal(quote: Quote): TradingSignal {
        val currentPrice = quote.currentPrice
        val output = m_Algorithm.run(m_Holdings, m_CurrentCapital, currentPrice)

        return TradingSignal(
            output.buy,
            output.sell,
            currentPrice
        )
    }

    //===========================================================//
    /**
     * Applies a successfully executed buy order.
     *
     * This method should only be called after the trading service has confirmed
     * that the buy order was executed successfully.
     *
     * @param buy the buy order that has been accepted and finalized.
     */
    fun finalizeOrder(buy: TradingAlgorithm.Output.Buy, buyPrice: Double) {
        m_CurrentCapital -= buy.amount * buyPrice
        m_Holdings.add(SecurityHolding(
            buyPrice,
            buy.amount
        ))
    }

    //===========================================================//
    /**
     * Applies a successfully executed sell order.
     *
     * This method should only be called after the trading service has confirmed
     * that the sell order was executed successfully.
     *
     * @param sell the sell order that has been accepted and finalized.
     */
    fun finalizeOrder(sell: TradingAlgorithm.Output.Sell, sellPrice: Double) {
        sell.batches.forEach{ batch ->
            val holding = batch.first
            val amountToSell = batch.second

            m_Holdings.remove(holding)

            if(amountToSell != holding.amount){
                m_Holdings.add(SecurityHolding(
                    holding.entryPrice,
                    holding.amount - amountToSell
                ))
            }

            m_CurrentCapital += amountToSell * sellPrice
        }
    }

    //===========================================================//

    fun setAlgorithm(algorithm: TradingAlgorithm.Type) {
        m_Algorithm = TradingAlgorithm.create(algorithm, securityIdentifier)
    }

    //===========================================================//

    fun getHoldings(): List<SecurityHolding> {
        return m_Holdings
    }

    //===========================================================//

    fun changeCurrentCapital(capital: Double) {
        m_CurrentCapital += capital;
    }

    //===========================================================//

    fun getCurrentCapital(): Double {
        return m_CurrentCapital
    }

    //===========================================================//
    //===========================================================//
    // Constructor(s)

    /**
     * @param securityIdentifier the identifier of the traded security.
     * @param holdings the currently held securities with the given identifier.
     * @param allocatedCapital the capital currently allocated to the trader.
     * @param algorithmType the type of the algorithm used to create trades.
     * */
    constructor(securityIdentifier: SecurityIdentifier, holdings: MutableList<SecurityHolding>, allocatedCapital: Double, algorithmType: TradingAlgorithm.Type) {
        this.securityIdentifier = securityIdentifier
        m_Holdings = holdings
        m_CurrentCapital = allocatedCapital
        m_Algorithm = TradingAlgorithm.create(algorithmType, securityIdentifier)
    }
}