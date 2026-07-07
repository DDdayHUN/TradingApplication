package domain.interfaces

import domain.market.security.SecurityIdentifier
import domain.trader.Trader

//===========================================================//
//===========================================================//

interface ITraderRepository {
    //===========================================================//
    //===========================================================//

    suspend fun save(trader: Trader)

    //===========================================================//

    suspend fun getBySecurityIdentifier(securityIdentifier: SecurityIdentifier): Trader

    //===========================================================//

    suspend fun getAll(): List<Trader>
}