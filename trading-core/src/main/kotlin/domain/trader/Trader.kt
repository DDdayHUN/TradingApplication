package domain.trader

import domain.algorithm.ITradingAlgorithm
import domain.market.Quote
import domain.market.security.SecurityHolding
import domain.market.security.SecurityIdentifier
import application.service.broker.InteractiveBrokersOrder
import java.util.*

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

    val id: UUID
    val securityIdentifier: SecurityIdentifier

    val capital: Double get() = m_Capital
    val holdings: Set<SecurityHolding> get() = m_Holdings.toSet()
    var algorithm: ITradingAlgorithm

    //===========================================================//
    //===========================================================//
    // Private Field(s)

    private var m_Capital: Double
    private val m_Holdings: MutableSet<SecurityHolding>

    //===========================================================//
    //===========================================================//
    // Public Method(es)

    fun createOrder(quote: Quote): TradingOrder {
        val currentPrice = quote.currentPrice
        val output = algorithm.run(holdings, capital, currentPrice)

        return TradingOrder(
            traderId = id,
            signal = output,
            atPrice = currentPrice
        )
    }

    //===========================================================//

    fun applyBuyFill(price: Double, amount: Int) {
        buy(
            price = price,
            amount = amount
        )
    }

    //===========================================================//

    fun applySellFill(price: Double, holdingsToSell: Set<SellHolding>) {
        holdingsToSell.forEach { item ->
            val holding = m_Holdings.find { it.id == item.id }
                ?: throw IllegalStateException("Holding ${item.id} not found")

            sell(
                holding = holding,
                price = price,
                amount = item.amount
            )
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

    fun allocatedValue(): Double {
        return m_Capital + m_Holdings.sumOf { holding ->
            holding.purchasePrice + holding.amount
        }
    }

    //===========================================================//

    fun changeAlgorithm(algorithm: ITradingAlgorithm) {
        this.algorithm = algorithm
    }

    //===========================================================//
    //===========================================================//
    // Private Method(es)

    private fun buy(price: Double, amount: Int) {

        changeCapital(-(amount * price))

        m_Holdings.add(
            SecurityHolding(
                purchasePrice = price,
                amount = amount,
            )
        )
    }

    //===========================================================//

    private fun sell(holding: SecurityHolding, price: Double, amount: Int) {
        changeCapital(price * amount)

        m_Holdings.remove(holding)
        if (amount < holding.amount) {
            m_Holdings.add(
                SecurityHolding(
                    holding.id,
                    holding.timestamp,
                    holding.purchasePrice,
                    holding.amount - amount
                )
            )
        }
    }

    //===========================================================//
    //===========================================================//
    // Constructor(s)

    /**
     * @param id the UUID of the Trader.
     * @param securityIdentifier the identifier of the traded security.
     * @param holdings the currently held securities with the given identifier.
     * @param allocatedCapital the capital currently allocated to the trader.
     * @param algorithm the algorithm instance with which we create trades.
     */
    constructor(id: UUID = UUID.randomUUID(), securityIdentifier: SecurityIdentifier, holdings: MutableSet<SecurityHolding> = mutableSetOf(), allocatedCapital: Double = 0.0, algorithm: ITradingAlgorithm) {
        this.id = id
        this.securityIdentifier = securityIdentifier
        m_Holdings = holdings
        m_Capital = allocatedCapital
        this.algorithm = algorithm
    }
}