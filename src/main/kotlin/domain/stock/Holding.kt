package domain.stock

//===========================================================//

/**
 * Represents a stock holding in a portfolio.
 * 
 * @param entryPrice - the average purchase price per share.
 * @param amount - the number of shares held.
 */
//===========================================================//

data class Holding(
    val entryPrice: Double,
    val amount: Long
) {
    init {
        require(entryPrice >= 0.0) { "Price" }
        require(amount != 0L) { "Amount" }
    }
}
