package domain.interfaces

import domain.market.security.SecurityIdentifier
import domain.market.Quote

//===========================================================//
//===========================================================//

interface IMarketDataProvider {
    //===========================================================//
    //===========================================================//

    suspend fun getQuote(identifier: SecurityIdentifier): Result<Quote>
}