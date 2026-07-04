package infrastructure.network

//===========================================================//
/**
 * Represents the latest market quote for the given asset.
 *
 * @param currentPrice The current price of the traded asset.
 */
//===========================================================//

data class Quote(
    val currentPrice: Double,
) {
    init {
        require(currentPrice >= 0.0) { "Current price is invalid" }
    }
}