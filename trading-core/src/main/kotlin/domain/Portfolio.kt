package domain

import domain.trader.Trader
import java.util.UUID

//===========================================================//
//===========================================================//

class Portfolio {
    //===========================================================//
    //===========================================================//
    // Public Field(s)

    val id: UUID
    val traders: Set<Trader> get() = m_Traders.toSet()
    val capital: Double get() = m_Capital

    //===========================================================//
    //===========================================================//
    // Private Field(s)

    private val m_Traders: MutableSet<Trader>
    private var m_Capital: Double

    //===========================================================//
    //===========================================================//
    // Public Method(es)

    fun addTrader(trader: Trader) {
        m_Traders.add(trader)
    }

    //===========================================================//

    fun removeTrader(trader: Trader) {
        m_Traders.remove(trader)
    }

    //===========================================================//

    fun changeCapital(capital: Double) {
        if(capital < 0.0) require(m_Capital + capital >= 0.0) { "Capital must be greater or equal to 0 after change" }
        m_Capital += capital
    }

    //===========================================================//
    //===========================================================//
    // Constructor(s)

    constructor(id: UUID = UUID.randomUUID(), traders: MutableSet<Trader> = HashSet(), capital: Double = 0.0) {
        this.id = id
        this.m_Traders = traders
        this.m_Capital = capital
    }
}