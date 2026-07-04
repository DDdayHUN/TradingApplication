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
    val capital: Double get() = m_Capital
    val holdings: List<SecurityHolding> get() = m_Holdings

    //===========================================================//
    //===========================================================//
    // Private Field(s)

    private var m_Capital: Double
    private val m_Holdings: MutableList<SecurityHolding>
    private var m_Algorithm: ITradingAlgorithm

    //===========================================================//
    //===========================================================//
    // Public Method(es)

    fun createSignal(quote: Quote): TradingSignal {
        val currentPrice = quote.currentPrice
        val output = m_Algorithm.run(holdings, capital, currentPrice)

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
        buy(buyPrice, buy.amount)
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
            sell(holding, sellPrice, amountToSell)
        }
    }

    //===========================================================//

    fun setAlgorithm(algorithm: TradingAlgorithm.Type) {
        m_Algorithm = TradingAlgorithm.create(algorithm, securityIdentifier)
    }

    //===========================================================//

    fun changeCapital(capital: Double) {
        if(capital < 0.0) require(m_Capital + capital >= 0.0) { "Capital must be greater or equal to 0 after change" }
        m_Capital += capital
    }

    //===========================================================//

    fun equity(currentPrice: Double): Double {
        return m_Capital + m_Holdings.sumOf { it.amount * currentPrice }
    }

    //===========================================================//
    //===========================================================//
    // Private Method(es)

    private fun buy(price: Double, amount: Long) {
        require(amount * price <= m_Capital) { "Insufficient Capital" }
        m_Holdings.add(
            SecurityHolding(
                price,
                amount,
            )
        )
    }

    //===========================================================//

    private fun sell(holding: SecurityHolding, price: Double, amount: Long) {
        require(amount <= holding.amount) { "Amount" }
        require(m_Holdings.remove(holding)) { "Not contained in the holdings list" }

        m_Capital += amount * price

        if (amount != holding.amount) {
            m_Holdings.add(
                SecurityHolding(
                    holding.entryPrice,
                    holding.amount - amount
                )
            )
        }
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
        m_Capital = allocatedCapital
        m_Algorithm = TradingAlgorithm.create(algorithmType, securityIdentifier)
    }
}