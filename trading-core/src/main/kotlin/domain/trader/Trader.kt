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

        val order = TradingOrder(
            traderId = id,
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
    @Deprecated("Will be removed in the future")
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

    fun applyBuyFill(price: Double, amount: Int) {
        buy(
            price = price,
            amount = amount
        )
    }

    //===========================================================//

    fun applySellFill(
        price: Double,
        batches: List<Pair<SecurityHolding, Int>>
    ) {
        batches.forEach { batch ->
            val requestedHolding = batch.first
            val amountToSell = batch.second

            val actualHolding = m_Holdings.find {
                it.id == requestedHolding.id
            } ?: throw IllegalStateException(
                "Holding ${requestedHolding.id} not found"
            )

            sell(
                holding = actualHolding,
                price = price,
                amount = amountToSell
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

    fun changeAlgorithm(algorithm: ITradingAlgorithm) {
        this.algorithm = algorithm
    }

    //===========================================================//
    //===========================================================//
    // Private Method(es)

    private fun buy(price: Double, amount: Int) {
        require(amount * price <= m_Capital) { "Insufficient Capital" }

        changeCapital(-(amount * price))

        m_Holdings.add(
            SecurityHolding(
                entryPrice = price,
                amount = amount,
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
                    holding.id,
                    holding.timestamp,
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
     * @param id the UUID of the Trader.
     * @param securityIdentifier the identifier of the traded security.
     * @param holdings the currently held securities with the given identifier.
     * @param allocatedCapital the capital currently allocated to the trader.
     * @param algorithm the algorithm instance with which we create trades.
     */
    constructor(id: UUID = UUID.randomUUID(), securityIdentifier: SecurityIdentifier, holdings: MutableSet<SecurityHolding> = mutableSetOf(), allocatedCapital: Double, algorithm: ITradingAlgorithm) {
        this.id = id
        this.securityIdentifier = securityIdentifier
        m_Holdings = holdings
        m_Capital = allocatedCapital
        this.algorithm = algorithm
    }
}