package data.network.ibkr

import domain.interfaces.IMarketDataProvider
import domain.market.Quote
import domain.market.security.SecurityIdentifier
import infrastructure.broker.IbkrClient

class IbkrMarketDataProvider(
    private val ibkrClient: IbkrClient
): IMarketDataProvider {
    override suspend fun getQuote(identifier: SecurityIdentifier): Result<Quote> {
        return runCatching {
            val price = ibkrClient.getCurrentPrice(
                ticker = identifier.tickerSymbol,
                currency = identifier.currency
            )

            Quote(
                currentPrice = price
            )
        }
    }
}