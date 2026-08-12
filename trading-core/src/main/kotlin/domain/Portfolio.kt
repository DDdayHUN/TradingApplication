package domain

import domain.trader.Trader
import domain.trader.TradingOrder
import java.util.UUID

//===========================================================//
//===========================================================//

class Portfolio {
    //===========================================================//
    //===========================================================//
    // Public Field(s)

    val id: UUID
    val userId: UUID
    val traders: List<Trader> get() = m_Traders.toList()
    val capital: Double get() = m_Capital

    //===========================================================//
    //===========================================================//
    // Private Field(s)

    private val m_Traders: MutableList<Trader>
    private var m_Capital: Double

    //===========================================================//
    //===========================================================//
    // Public Method(es)

    fun addTrader(trader: Trader) {
        m_Traders.add(trader)
    }

    //===========================================================//

    /**
     * @return `true` if the trader has been successfully removed; `false` if it was not contained in the collection.
     */
    fun removeTrader(trader: Trader): Boolean {
        return m_Traders.remove(trader)
    }

    //===========================================================//

    fun changeCapital(capital: Double) {
        if(capital < 0.0) require(m_Capital + capital >= 0.0) { "Capital must be greater or equal to 0 after change" }
        m_Capital += capital
    }

    //===========================================================//
    //===========================================================//
    // Constructor(s)

    constructor(id: UUID = UUID.randomUUID(), userId: UUID, traders: MutableList<Trader> = ArrayList(), capital: Double = 0.0) {
        this.id = id
        this.userId = userId
        this.m_Traders = traders
        this.m_Capital = capital
    }
}