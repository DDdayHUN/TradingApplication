package data.network.ibkr

import data.network.IMarketDataProvider
import domain.market.Quote
import domain.market.security.SecurityIdentifier
import infrastructure.broker.InteractiveBrokerSessionManager
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class IbkrMarketDataProvider(
    private val session: InteractiveBrokerSessionManager
): IMarketDataProvider {
    //===========================================================//
    //===========================================================//
    // Public Method(s)

    override suspend fun getQuote(userId: UUID, identifier: SecurityIdentifier): Result<Quote> {
        return runCatching {
            val client = session.getClient(userId)

            val price = client.getCurrentPrice(
                ticker = identifier.tickerSymbol,
                currency = identifier.currency
            )

            Quote(
                currentPrice = price
            )
        }
    }
}