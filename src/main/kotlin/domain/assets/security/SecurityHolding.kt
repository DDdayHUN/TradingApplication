package domain.assets.security

import java.util.UUID

//===========================================================//
/**
 * Represents a stock holding in a portfolio.
 * 
 * @param entryPrice the average purchase price per share.
 * @param amount the number of shares held.
 */
//===========================================================//

data class SecurityHolding(
    val uuid: UUID,
    val entryPrice: Double,
    val amount: Int
) {
    constructor(entryPrice: Double, amount: Int) : this(UUID.randomUUID(), entryPrice, amount)

    init {
        require(entryPrice >= 0.0) { "Price" }
        require(amount >= 0) { "Amount must be positive" }
    }
}
