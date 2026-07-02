package data

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import domain.assets.security.SecurityHistory
import domain.assets.security.SecurityHolding
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

    //===========================================================//
    //===========================================================//
    // Public Interface(s)

    /**
     * Loads historical data from files based on the given parameters.
     *
     * @param stockName Stock name.
     * @param from Start date (inclusive).
     * @param to End date (inclusive).
     * @return List of historical data entries.
     */
    @Deprecated("This only used for old serialized data")
    fun loadHistoryForAlgorithms(stockName: String, from: Int, to: Int): MutableList<SecurityHistory> {
        val backtestFiles = File("src/main/resources/backtest/old/us/").listFiles() ?: error("Backtest directory is missing or not a directory")
        val proxy: MutableList<Pair<File, Int>> = ArrayList()

        for (file in backtestFiles) {
            val nameWithoutExtension = file.nameWithoutExtension
            val date = Integer.parseInt(nameWithoutExtension.substring(nameWithoutExtension.length - 2))
            val nameWithoutExtensionAndDate = nameWithoutExtension.substring(0, nameWithoutExtension.length - 2)

            if (date in from..to && nameWithoutExtensionAndDate == stockName) proxy.add(Pair(file, date))
        }


        proxy.sortWith(Comparator.comparingInt { h -> h.second }) // Sort chronologically by the date
        // If 'from' and/or 'to' are MIN and/or MAX then we've loaded all, so we don't throw.
        require(!(from != Integer.MIN_VALUE && to != Integer.MAX_VALUE && proxy.size != (to - from + 1))) { "From-To" }

        val ret: MutableList<SecurityHistory> = ArrayList()
        for (item in proxy) {
            val serData = SerializationManager.loadFromFileForBackTest(item.first)
            for (item2 in serData.stockHistory.entries) {
                ret.addAll(item2.value)
            }
        }

        return ret
    }

    //===========================================================//
    /**
     * Loads historical data from files based on the given parameters for backtesting algorithms.
     *
     * @param securityName the name of the security.
     * @param from the start date from which we want to include historical data (inclusive).
     * @param to the end date from which we don't want to include historical data (inclusive).
     * @return List of historical data entries.
     */
    fun loadBackTestData(securityName: String, from: Instant, to: Instant): MutableList<SecurityHistory> {
        val allBacktestFiles: Array<File> = File("src/main/resources/backtest/us/").listFiles() ?: error("Backtest directory is missing or not a directory")
        val backtestFile = allBacktestFiles.single { it.nameWithoutExtension == securityName }
        val data = loadBacktestDataFromFile(backtestFile)
        return data.history.filter { it.date in from..to }.map { SecurityHistory(it.closingPrice) }.toMutableList()
    }

    //===========================================================//
    //===========================================================//
    // Private Interface(s)

    private fun loadBacktestDataFromFile(file: File): SecuritySerializationData {
        InputStreamReader(FileInputStream(file), StandardCharsets.UTF_8).use { reader ->
            return s_GSON.fromJson(reader, SecuritySerializationData::class.java)
        }
    }

    //===========================================================//

    @Deprecated("This only used for old serialized data")
    private fun loadFromFileForBackTest(file: File): SerializationData {
        InputStreamReader(FileInputStream(file), StandardCharsets.UTF_8).use { reader ->
            return s_GSON.fromJson(reader, SerializationData::class.java)
        }
    }

    //===========================================================//
    //===========================================================//
    // Helper Class(es)

    @Deprecated("This only used for old serialized data")
    data class SerializationData(
        val stockHistory: Map<String, List<SecurityHistory>>,
        val holdings: Map<String, List<SecurityHolding>>
    )
}
