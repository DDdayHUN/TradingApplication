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

    val availableCapital: Double get() = m_AvailableCapital
    val netLiquidation: Double get() = m_AvailableCapital +
            traders.sumOf { it.availableCapital } +
            traders.sumOf { trader -> trader.holdings.sumOf { it.purchasePrice * it.amount } }
    // NOTE : Net liquidation is not calculated from past price but rather from the current price no?

    //===========================================================//
    //===========================================================//
    // Private Field(s)

    private val m_Traders: MutableSet<Trader>
    private var m_AvailableCapital: Double

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

    constructor(id: UUID = UUID.randomUUID(), traders: MutableSet<Trader> = HashSet(), availableCapital: Double = 0.0) {
        this.id = id
        this.m_Traders = traders
        this.m_AvailableCapital = availableCapital
    }
}