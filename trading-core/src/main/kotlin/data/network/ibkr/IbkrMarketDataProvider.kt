package data.network.ibkr

import data.network.IMarketDataProvider
import domain.market.Quote
import domain.market.security.SecurityIdentifier
import infrastructure.broker.InteractiveBrokersSession

class IbkrMarketDataProvider(
    private val session: InteractiveBrokersSession
): IMarketDataProvider {
    override suspend fun getQuote(identifier: SecurityIdentifier): Result<Quote> {
        return runCatching {
            val client = session.getClient()

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