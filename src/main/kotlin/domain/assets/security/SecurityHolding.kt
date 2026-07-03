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

class SecurityHolding private constructor(
    val uuid: UUID,
    val entryPrice: Double,
    val amount: Long
) {
    override fun toString(): String {
        return "SecurityHolding(uuid=$uuid, entryPrice=$entryPrice, amount=$amount)"
    }

    constructor(entryPrice: Double, amount: Long) : this(UUID.randomUUID(), entryPrice, amount)

    init {
        require(entryPrice >= 0.0) { "Price" }
        require(amount != 0L) { "Amount" }
    }
}
