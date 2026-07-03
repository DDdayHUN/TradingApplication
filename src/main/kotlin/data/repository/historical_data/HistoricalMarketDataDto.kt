package data.repository.historical_data

import kotlin.time.Instant

internal data class HistoricalMarketDataDto(
    val meta: Meta,
    val history: List<MarketHistory>
) {
    data class Meta(
        val isin: String,
        val exchange: String,
        val tickerSymbol: String,
        val currency: String,
    )

    data class MarketHistory(
        val date: Instant,
        val closingPrice: Double,
    )
}