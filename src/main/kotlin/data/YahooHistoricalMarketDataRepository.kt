package data

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import domain.assets.security.SecurityIdentifier
import java.io.File
import kotlin.time.Instant

<<<<<<<< HEAD:src/main/kotlin/data/YahooHistoricalMarketDataRepository.kt
internal object YahooHistoricalMarketDataRepository : SerializationManager() {
========
internal object YahooSerializedMarketDataParser : SerializationManager() {
>>>>>>>> 3d085313f4b75a377af4ecbcbd439ebfa7d39814:src/main/kotlin/data/YahooSerializedMarketDataParser.kt
    //===========================================================//
    //===========================================================//
    // Private Field(s)

    private val s_GSON: Gson = GsonBuilder()
        .setPrettyPrinting()
        .create()

    //===========================================================//
    //===========================================================//
    // Public Method(es)

    override fun parse(securityIdentifier: SecurityIdentifier): SecuritySerializationData {
        val rootDir = File("src/main/resources/backtest/yahoo/")
        val targetFile = rootDir.walkTopDown()
            .filter { it.isFile }
            .find { file ->
                val yahooMarketData = loadFromFile<YahooMarketData>(s_GSON, file)
                yahooMarketData.isin == securityIdentifier.isin
            }

        require(targetFile != null) { "There is no files with the given identifier" }
        return loadFromFile<YahooMarketData>(s_GSON, targetFile).toSecuritySerializationData()
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
