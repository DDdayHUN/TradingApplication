package data

import domain.assets.security.SecurityHistory
import domain.assets.security.SecurityHolding
import kotlin.time.Instant

@Deprecated("This only used for old serialized data")
data class SerializationData(
    val stockHistory: Map<String, List<SecurityHistory>>,
    val holdings: Map<String, List<SecurityHolding>>
)

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
        val date: Instant,
        val closingPrice: Double,
    )
}