package domain.market.security

import kotlin.time.Instant

//===========================================================//
/**
 * Represents the historical data of a stock for a single trading day.
 * 
 * @param closingPrice the closing price of the asset.
 * @param timestamp the exact timestamp when this history snapshot was made at.
 */
//===========================================================//

data class SecurityHistory(
    val closingPrice: Double,
    val timestamp: Instant
) {
    init {
        require(closingPrice >= 0.0) { "ClosingPrice" }
    }
}
