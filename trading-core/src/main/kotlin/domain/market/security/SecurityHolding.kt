package domain.market.security

import com.google.gson.annotations.JsonAdapter
import domain.adapter.InstantAdapter
import java.time.Instant
import java.util.*

//===========================================================//
/**
 * Represents a stock holding in a portfolio.
 *
 * @param id the uuid of the given holding.
 * @param timestamp the time at which the holding was acquired.
 * @param purchasePrice the average purchase price of the shares.
 * @param amount the number of shares held.
 */
//===========================================================//

data class SecurityHolding(
    val id: UUID = UUID.randomUUID(),
    @JsonAdapter(InstantAdapter::class)
    val timestamp: Instant = Instant.now(),
    val purchasePrice: Double,
    val amount: Int
) {
    init {
        require(purchasePrice >= 0.0) { "Price" }
        require(amount > 0) { "Amount must be greater than 0" }
    }
}