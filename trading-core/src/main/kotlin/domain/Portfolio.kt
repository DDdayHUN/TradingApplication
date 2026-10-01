package domain

import domain.trader.Trader
import exception.api.TraderHoldingsNotEmptyException
import java.util.*

//===========================================================//
//===========================================================//

class Portfolio {
    //===========================================================//
    //===========================================================//
    // Public Field(s)

    val id: UUID

    val traders: Set<Trader> get() = m_Traders.toSet()
    val availableCapital: Double get() = traders.sumOf { it.availableCapital }
    val allocatedCapital: Double get() = traders.sumOf { it.allocatedCapital }

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
        if(trader.holdings.isNotEmpty()) throw TraderHoldingsNotEmptyException(trader.id)
        m_Traders.remove(trader)
    }

    //===========================================================//
    //===========================================================//
    // Constructor(s)

    constructor(id: UUID = UUID.randomUUID(), traders: MutableSet<Trader> = HashSet()) {
        this.id = id
        this.m_Traders = traders
    }
}