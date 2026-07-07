package domain.trader

import domain.algorithm.ITradingAlgorithm
import domain.market.Quote
import domain.market.security.SecurityHolding
import domain.market.security.SecurityIdentifier
import java.util.UUID

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

    val uuid: UUID
    val securityIdentifier: SecurityIdentifier

    val capital: Double get() = m_Capital
    val holdings: List<SecurityHolding> get() = m_Holdings.toList()
    var algorithm: ITradingAlgorithm

    //===========================================================//
    //===========================================================//
    // Private Field(s)

    private var m_Capital: Double
    private val m_Holdings: MutableList<SecurityHolding>

    //===========================================================//
    //===========================================================//
    // Public Method(es)

    fun createOrder(quote: Quote): TradingOrder {
        val currentPrice = quote.currentPrice
        val output = algorithm.run(holdings, capital, currentPrice)

        val order = TradingOrder(
            traderUuid = uuid,
            securityIdentifier = securityIdentifier,
            buy = output.buy,
            sell = output.sell,
            atPrice = currentPrice,
        )

        return order
    }

    //===========================================================//
    /**
     * Applies a successfully executed order.
     *
     * This method should only be called after the trading data.service has confirmed
     * that the buy order was executed successfully.
     *
     * @param order the order that has been accepted and should be finalized.
     */
    fun finalizeOrder(order: TradingOrder) {
        if(order.buy != null) buy(order.atPrice, order.buy.amount)
        if(order.sell != null) {
            order.sell.batches.forEach{ batch ->
                val holding = batch.first
                val amountToSell = batch.second
                sell(holding, order.atPrice, amountToSell)
            }
        }
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

    private fun buy(price: Double, amount: Int) {
        require(amount * price <= m_Capital) { "Insufficient Capital" }

        changeCapital(-(amount * price))

        m_Holdings.add(
            SecurityHolding(
                price,
                amount,
            )
        )
    }

    //===========================================================//

    private fun sell(holding: SecurityHolding, price: Double, amount: Int) {
        require(amount <= holding.amount) { "Amount" }
        require(m_Holdings.remove(holding)) { "Not contained in the holdings list" }

        changeCapital(price * amount)

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
     * @param algorithm the algorithm instance with which we create trades.
     */
    constructor(uuid: UUID = UUID.randomUUID(), securityIdentifier: SecurityIdentifier, holdings: MutableList<SecurityHolding>, allocatedCapital: Double, algorithm: ITradingAlgorithm) {
        this.uuid = uuid
        this.securityIdentifier = securityIdentifier
        m_Holdings = holdings
        m_Capital = allocatedCapital
        this.algorithm = algorithm
    }
}