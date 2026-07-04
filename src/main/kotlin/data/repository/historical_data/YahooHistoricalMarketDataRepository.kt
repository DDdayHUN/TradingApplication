package data.repository.historical_data

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import data.repository.util.RepositoryUtil
import domain.assets.security.SecurityIdentifier
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.time.Instant

internal object YahooHistoricalMarketDataRepository : IHistoricalMarketDataRepository {
    //===========================================================//
    //===========================================================//
    // Private Field(s)

    private val s_RootDir = File("src/main/resources/backtest/yahoo/")

    private val s_GSON: Gson = GsonBuilder()
        .setPrettyPrinting()
        .create()

    //===========================================================//
    //===========================================================//
    // Public Method(es)

    override suspend fun getBySecurityIdentifier(securityIdentifier: SecurityIdentifier): HistoricalMarketDataDto = withContext(Dispatchers.IO) {
        val targetFile = s_RootDir.walkTopDown()
            .filter { it.isFile }
            .find { file ->
                val yahooMarketData = RepositoryUtil.loadFromFile<YahooMarketData>(s_GSON, file)
                yahooMarketData.isin == securityIdentifier.isin
            }

        require(targetFile != null) { "There is no file with the given identifier" }
        return@withContext RepositoryUtil.loadFromFile<YahooMarketData>(s_GSON, targetFile).toSecuritySerializationData()
    }

    //===========================================================//

    override suspend fun getAll(): List<HistoricalMarketDataDto> = withContext(Dispatchers.IO) {
        val files = s_RootDir
            .walkTopDown()
            .filter { it.isFile }
            .toList()

        coroutineScope {
            files.map {
                async {
                    RepositoryUtil
                        .loadFromFile<YahooMarketData>(s_GSON, it)
                        .toSecuritySerializationData()
                }
            }.awaitAll()
        }
    }

    //===========================================================//
    //===========================================================//
    // Helper Class(es)

    private data class YahooMarketData(
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
                    val adjclose: List<Double?>
                )
            }
        }

        fun toSecuritySerializationData(): HistoricalMarketDataDto {
            val timestamps = result.timestamp
            val closes = result.indicators.adjclose.first().adjclose

            val history = timestamps.zip(closes).mapNotNull { (time, price) ->
                if(price == null){
                    null
                }else {
                    HistoricalMarketDataDto.MarketHistory(
                        date = Instant.fromEpochSeconds(time),
                        closingPrice = price
                    )
                }
            }

            return HistoricalMarketDataDto(
                meta = HistoricalMarketDataDto.Meta(
                    isin,
                    result.meta.fullExchangeName,
                    result.meta.symbol,
                    result.meta.currency,
                ),
                history = history
            )
        }
    }
}