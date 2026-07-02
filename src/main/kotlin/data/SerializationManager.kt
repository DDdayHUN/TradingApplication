package data

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import domain.assets.security.SecurityHistory
import domain.assets.security.SecurityHolding
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.nio.charset.StandardCharsets

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
    //===========================================================//
    // Private Interface(s)

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
