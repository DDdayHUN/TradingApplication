package domain

import java.util.UUID

//===========================================================//
//===========================================================//

class User {
    //===========================================================//
    //===========================================================//
    // Public Field(s)

    val id: UUID
    val portfolios: Set<Portfolio> get() = m_Portfolios.toSet()
    val userName: String

    //===========================================================//
    //===========================================================//
    // Private Field(s)

    private val m_Portfolios: MutableSet<Portfolio>

    //===========================================================//
    //===========================================================//
    // Public Method(es)

    fun addPortfolio(portfolio: Portfolio) {
        m_Portfolios.add(portfolio)
    }

    //===========================================================//

    fun removePortfolio(portfolio: Portfolio) {
        m_Portfolios.remove(portfolio)
    }

    //===========================================================//
    //===========================================================//
    // Constructor(s)

    constructor(id: UUID = UUID.randomUUID(), portfolios: MutableSet<Portfolio> = HashSet(), userName: String) {
        this.id = id
        this.m_Portfolios = portfolios
        this.userName = userName
    }
}