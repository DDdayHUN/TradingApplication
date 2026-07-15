package domain.market.security

//===========================================================//
/**
 * Represents the historical data of a stock for a single trading day.
 * 
 * @param closingPrice the closing price of the asset.
 */
//===========================================================//

data class SecurityHistory(
    val closingPrice: Double
) {
    init {
        require(closingPrice >= 0.0) { "ClosingPrice" }
    }
}
