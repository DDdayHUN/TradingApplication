package data

import com.google.gson.Gson
import domain.assets.security.SecurityHistory
import domain.assets.security.SecurityIdentifier
import java.io.File
import java.io.FileInputStream
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets
import kotlin.time.Instant

sealed class SerializationManager {
    companion object {
        //===========================================================//
        //===========================================================//
        // Private Field(s)

        private val s_MarketDataParser = get(Type.YahooMarketDataParser)

        //===========================================================//
        //===========================================================//
        // Public Method(s)

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

        //===========================================================//

        internal inline fun <reified T> loadFromFile(serializer: Gson, file: File): T {
            InputStreamReader(FileInputStream(file), StandardCharsets.UTF_8).use { reader ->
                return serializer.fromJson(reader, T::class.java)
            }
        }

        //===========================================================//
        //===========================================================//
        // Private Field(s)

        private fun get(type: Type): SerializationManager {
            return when (type) {
                Type.YahooMarketDataParser -> {
                    YahooSerializedMarketDataParser
                }
            }
        }
    }

    //===========================================================//

    internal abstract fun parse(securityIdentifier: SecurityIdentifier): SecuritySerializationData

    //===========================================================//
    //===========================================================//
    // Helper Class(es)

    sealed interface Type {
        data object YahooMarketDataParser : Type
    }
}
