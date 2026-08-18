package domain.market.security

import com.google.gson.annotations.JsonAdapter
import domain.adapter.InstantAdapter
import java.time.Instant
import java.util.UUID

//===========================================================//
/**
 * Represents a stock holding in a portfolio.
 *
 * @param id the uuid of the given holding.
 * @param timestamp the time at which the holding was acquired.
 * @param entryPrice the average purchase price per share.
 * @param amount the number of shares held.
 */
//===========================================================//

data class SecurityHolding(
    val id: UUID = UUID.randomUUID(),
    @JsonAdapter(InstantAdapter::class)
    val timestamp: Instant = Instant.now(),
    val entryPrice: Double,
    val amount: Int
) {
    init {
        require(entryPrice >= 0.0) { "Price" }
        require(amount > 0) { "Amount must be greater than 0" }
    }
}