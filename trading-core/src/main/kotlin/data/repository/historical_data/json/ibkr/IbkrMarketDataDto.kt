package data.repository.historical_data.json.ibkr

import data.repository.historical_data.HistoricalMarketDataDto
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import kotlin.time.toKotlinInstant

internal data class IbkrMarketDataDto(
    val isin: String,
    val list: List<Item>
) {
    data class Item(
        val timestamp: String,
        val price: Double
    )

    companion object {
        // Formatter for "20250829 09:40:00 US/Eastern"
        private val TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd HH:mm:ss VV")!!
    }

    fun toHistoricalMarketDataDto(): HistoricalMarketDataDto {
        val history = list.map { item ->
            item.price.let { price ->
                HistoricalMarketDataDto.MarketHistory(
                    date = ZonedDateTime.parse(item.timestamp, TIMESTAMP_FORMATTER).toInstant().toKotlinInstant(),
                    price = price
                )
            }
        }

        return HistoricalMarketDataDto(
            meta = HistoricalMarketDataDto.Meta(
                isin = isin,
                currency = "NaN",
                tickerSymbol = "NaN",
                exchange = "NaN"
            ),
            history = history
        )
    }
}