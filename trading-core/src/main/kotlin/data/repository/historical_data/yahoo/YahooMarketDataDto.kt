package data.repository.historical_data.yahoo

import data.repository.historical_data.HistoricalMarketDataDto
import kotlin.time.Instant

internal data class YahooMarketDataDto(
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

        fun toHistoricalMarketDataDto(): HistoricalMarketDataDto {
            val timestamps = result.timestamp
            val closes = result.indicators.adjclose.first().adjclose

            val history = timestamps.zip(closes).mapNotNull { (time, price) ->
                if(price == null) null
                else {
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