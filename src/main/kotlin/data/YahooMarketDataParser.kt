package data

import com.google.gson.GsonBuilder
import domain.assets.security.SecurityIdentifier
import java.io.File
import java.io.FileInputStream
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets
import kotlin.time.Instant

internal object YahooMarketDataParser : IMarketDataParser {
    //===========================================================//
    //===========================================================//
    // Private Field(s)

    private val s_GSON = GsonBuilder()
        .setPrettyPrinting()
        .create()

    //===========================================================//
    //===========================================================//
    // Public Method(es)

    override fun parse(securityIdentifier: SecurityIdentifier): SecuritySerializationData {
        val rootDir = File("src/main/resources/backtest/yahoo/")
        val targetFile = rootDir.walkTopDown()
            .filter { it.isFile } // Ignore directories, only look at files
            .find { file ->
                val yahooMarketData = loadFromFile(file)
                yahooMarketData.isin == securityIdentifier.isin
            }

        require(targetFile != null) { "There is no files with the given identifier" }
        return loadFromFile(targetFile).toSecuritySerializationData()
    }

    //===========================================================//
    //===========================================================//
    // Private Method(es)

    private fun loadFromFile(file: File): YahooMarketData {
        InputStreamReader(FileInputStream(file), StandardCharsets.UTF_8).use { reader ->
            return s_GSON.fromJson(reader, YahooMarketData::class.java)
        }
    }


    //===========================================================//
    //===========================================================//
    // Helper Class(es)

    internal data class YahooMarketData(
        val isin: String,
        val result: Result
    ) {
        data class Result(
            val meta: Meta,
            val timestamp: List<Long>,
            val indicators: Indicators,
        ) {
            data class Meta(
                val currency: String,
                val symbol: String,
                val exchangeName: String,
                val fullExchangeName: String,
                val instrumentType: String,
                val firstTradeDate: Long,
                val regularMarketTime: Long,
                val hasPrePostMarketData: Boolean,
                val gmtoffset: Int,
                val timezone: String,
                val exchangeTimezoneName: String,
                val regularMarketPrice: Double,
                val fiftyTwoWeekHigh: Double,
                val fiftyTwoWeekLow: Double,
                val regularMarketDayHigh: Double,
                val regularMarketDayLow: Double,
                val regularMarketVolume: Long,
                val longName: String,
                val shortName: String,
                val chartPreviousClose: Double,
                val priceHint: Int,
                val currentTradingPeriod: TradingPeriod,
                val dataGranularity: String,
                val range: String,
                val validRanges: List<String>
            ) {
                data class TradingPeriod(
                    val pre: Period,
                    val regular: Period,
                    val post: Period,
                ) {
                    data class Period(
                        val timezone: String,
                        val start: Long,
                        val end: Long,
                        val gmtoffset: Int
                    )
                }
            }

            data class Indicators(
                val quote: List<Quote>,
                val adjclose: List<AdjustedClose>,
            ) {
                data class Quote(
                    val open: List<Double>,
                    val close: List<Double>,
                    val low: List<Double>,
                    val volume: List<Long>,
                    val high: List<Double>,
                )

                data class AdjustedClose(
                    val adjclose: List<Double>
                )
            }
        }

        fun toSecuritySerializationData(): SecuritySerializationData {
            val timestamps = result.timestamp
            val closes = result.indicators.adjclose.first().adjclose

            val history = timestamps.zip(closes) { time, price ->
                SecuritySerializationData.MarketHistory(
                    date = Instant.fromEpochSeconds(time),
                    closingPrice = price
                )
            }

            return SecuritySerializationData(
                identifier = SecuritySerializationData.Identifier(
                    isin,
                    result.meta.fullExchangeName,
                    result.meta.symbol
                ),
                history = history
            )
        }
    }
}
