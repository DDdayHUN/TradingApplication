package data.repository.historical_data

import data.repository.historical_data.yahoo.YahooHistoricalMarketDataRepository
import domain.market.security.SecurityHistory
import domain.market.security.SecurityIdentifier
import domain.interfaces.IHistoricalMarketDataProvider
import kotlin.time.Instant

object HistoricalMarketDataProvider: IHistoricalMarketDataProvider {
    //===========================================================//
    //===========================================================//
    // Private Field(s)

    private val s_HistoricalMarketDataRepository = get(Type.YahooHistoricalMarketDataRepository)

    //===========================================================//
    //===========================================================//
    // Public Method(es)

    override suspend fun getBySecurityIdentifier(securityIdentifier: SecurityIdentifier, from: Instant, to: Instant): List<SecurityHistory> {
        val data = s_HistoricalMarketDataRepository.getBySecurityIdentifier(securityIdentifier)

        return data.history
            .filter { it.date in from..to }
            .sortedBy { it.date }
            .map { SecurityHistory(it.closingPrice) }
            .toMutableList()
    }

    //===========================================================//

    @Deprecated("We need to redo this, because this is too expensive")
    override suspend fun getAllSecurityIdentifiers(): List<SecurityIdentifier> {
        val data = s_HistoricalMarketDataRepository.getAll()
        return data.map {
            SecurityIdentifier(it.meta.isin, it.meta.tickerSymbol, it.meta.currency)
        }
    }

    //===========================================================//
    //===========================================================//
    // Private Method(es)

    private fun get(type: Type): IHistoricalMarketDataRepository {
        return when (type) {
            Type.YahooHistoricalMarketDataRepository -> {
                YahooHistoricalMarketDataRepository
            }
        }
    }

    //===========================================================//
    //===========================================================//
    // Helper Class(es)

    sealed interface Type {
        data object YahooHistoricalMarketDataRepository : Type
    }
}