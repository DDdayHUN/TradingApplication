package data

import data.repository.historical_data.IHistoricalMarketDataRepository
import data.repository.historical_data.YahooHistoricalMarketDataRepository
import domain.assets.security.SecurityHistory
import domain.assets.security.SecurityIdentifier
import kotlin.time.Instant

object HistoricalMarketDataProvider {
    //===========================================================//
    //===========================================================//
    // Private Field(s)

    private val s_HistoricalMarketDataRepository = get(Type.YahooHistoricalMarketDataRepository)

    //===========================================================//
    //===========================================================//
    // Public Method(s)

    /**
     * Loads historical data from files based on the given parameters for backtesting algorithms.
     *
     * @param securityIdentifier the identifier of the security.
     * @param from the start date from which we want to include historical data (inclusive).
     * @param to the end date from which we don't want to include historical data (inclusive).
     * @return the list of historical data entries.
     */
    suspend fun loadFromFile(securityIdentifier: SecurityIdentifier, from: Instant, to: Instant): List<SecurityHistory> {
        val data = s_HistoricalMarketDataRepository.getBySecurityIdentifier(securityIdentifier)

        return data.history
            .filter { it.date in from..to }
            .sortedBy { it.date }
            .map { SecurityHistory(it.closingPrice) }
            .toMutableList()
    }

    //===========================================================//
    //===========================================================//
    // Private Field(s)

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

    private sealed interface Type {
        data object YahooHistoricalMarketDataRepository : Type
    }
}