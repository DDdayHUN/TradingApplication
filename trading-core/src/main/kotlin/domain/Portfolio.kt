package domain

import domain.trader.Trader
import java.util.*

//===========================================================//
//===========================================================//

class Portfolio {
    //===========================================================//
    //===========================================================//
    // Public Field(s)

    val id: UUID
    val traders: Set<Trader> get() = m_Traders.toSet()

    //===========================================================//
    //===========================================================//
    // Private Field(s)

    private val m_Traders: MutableSet<Trader>

    //===========================================================//
    //===========================================================//
    // Public Method(es)

    fun addTrader(trader: Trader) {
        m_Traders.add(trader)
    }

    //===========================================================//

    fun removeTrader(trader: Trader) {
        require(trader.holdings.isEmpty()){ "Trader cannot be removed while it has open holdings" }
        m_Traders.remove(trader)
    }

    //===========================================================//

    fun allocatedCapital(): Double {
        return m_Traders.sumOf {trader ->
            trader.allocatedValue()
        }
    }


    //===========================================================//
    //===========================================================//
    // Constructor(s)

    constructor(id: UUID = UUID.randomUUID(), traders: MutableSet<Trader> = HashSet()) {
        this.id = id
        this.m_Traders = traders
    }
}