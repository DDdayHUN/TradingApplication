package data.repository.historical_data

import kotlin.time.Instant

internal data class HistoricalMarketData(
    val identifier: Identifier,
    val history: List<MarketHistory>
) {
    data class Identifier(
        val isin: String,
        val exchange: String,
        val tickerSymbol: String,
    )

    data class MarketHistory(
        val date: Instant,
        val closingPrice: Double,
    )
}