package domain.interfaces

import domain.market.security.SecurityIdentifier
import domain.trader.Trader

//===========================================================//
//===========================================================//

interface ITraderRepository {
    //===========================================================//
    //===========================================================//

    suspend fun save(trader: Trader): Result<Unit>

    //===========================================================//

    suspend fun getBySecurityIdentifier(securityIdentifier: SecurityIdentifier): Result<Trader>

    //===========================================================//

    suspend fun getAll(): Result<List<Trader>>
}