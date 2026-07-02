package data

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import domain.assets.security.SecurityHistory
import domain.assets.security.SecurityHolding
import domain.assets.security.SecurityIdentifier
import java.io.File
import java.io.FileInputStream
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets
import kotlin.time.Instant

//===========================================================//
//===========================================================//

object SerializationManager {
    //===========================================================//
    //===========================================================//
    // Private Field(s)

    private val s_GSON: Gson = GsonBuilder()
        .setPrettyPrinting()
        .create()

    private val s_MarketDataParser = IMarketDataParser.get(IMarketDataParser.Type.YahooMarketDataParser)

    //===========================================================//
    //===========================================================//
    // Public Interface(s)

    /**
     * Loads historical data from files based on the given parameters for backtesting algorithms.
     *
     * @param securityIdentifier the identifier of the security.
     * @param from the start date from which we want to include historical data (inclusive).
     * @param to the end date from which we don't want to include historical data (inclusive).
     * @return List of historical data entries.
     */
    fun loadHistoricalDataFromFile(securityIdentifier: SecurityIdentifier, from: Instant, to: Instant): MutableList<SecurityHistory> {
        val data = s_MarketDataParser.parse(securityIdentifier)

        return data.history
            .filter { it.date in from..to }
            .sortedBy { it.date }
            .map { SecurityHistory(it.closingPrice) }
            .toMutableList()
    }
}
