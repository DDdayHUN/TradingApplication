package data

data class SecuritySerializationData(
    val identifier: Identifier,
    val history: List<MarketHistory>
) {
    data class Identifier(
        val isin: String,
        val exchange: String,
        val tickerSymbol: String,
    )

    data class MarketHistory(
        val date: String,
        val closingPrice: Double,
    )
}