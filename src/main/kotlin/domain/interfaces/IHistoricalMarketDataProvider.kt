package domain.interfaces

import domain.market.security.SecurityHistory
import domain.market.security.SecurityIdentifier
import kotlin.time.Instant

//===========================================================//
//===========================================================//

interface IHistoricalMarketDataProvider {
    //===========================================================//
    //===========================================================//
    /**
     * Loads historical data from files based on the given parameters for backtesting algorithms.
     *
     * @param securityIdentifier the identifier of the security.
     * @param from the start date from which we want to include historical data (inclusive).
     * @param to the end date from which we don't want to include historical data (inclusive).
     * @return the list of historical data entries sorted by date.
     */
    suspend fun getBySecurityIdentifier(securityIdentifier: SecurityIdentifier, from: Instant, to: Instant): List<SecurityHistory>

    //===========================================================//
    /**
     * Extracts all available security identifiers from historical market data.
     *
     * This function loads all persisted historical market data from the repository and maps each entry
     * to a [SecurityIdentifier] using its metadata.
     *
     * @return a list of security identifiers derived from all stored market data entries.
     */
    suspend fun getAllSecurityIdentifiers(): List<SecurityIdentifier>
}