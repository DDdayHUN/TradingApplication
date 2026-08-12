package domain

import java.util.UUID

//===========================================================//
//===========================================================//

class User {
    //===========================================================//
    //===========================================================//
    // Public Field(s)

    val id: UUID

    //===========================================================//
    //===========================================================//
    // Private Field(s)

    private val m_Portfolios: MutableList<Portfolio>

    //===========================================================//
    //===========================================================//
    // Public Method(es)

    fun addPortfolio(portfolio: Portfolio) {
        m_Portfolios.add(portfolio)
    }

    //===========================================================//

    /**
     * @return `true` if the portfolio has been successfully removed; `false` if it was not contained in the collection.
     */
    fun removePortfolio(portfolio: Portfolio): Boolean {
        return m_Portfolios.remove(portfolio)
    }

    //===========================================================//
    //===========================================================//
    // Constructor(s)

    constructor(id: UUID = UUID.randomUUID(), portfolios: MutableList<Portfolio> = ArrayList()) {
        this.id = id
        this.m_Portfolios = portfolios
    }
}