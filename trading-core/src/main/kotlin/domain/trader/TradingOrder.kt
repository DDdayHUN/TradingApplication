package domain.trader

import domain.algorithm.TradingAlgorithm
import domain.market.security.SecurityIdentifier
import java.time.Instant
import java.util.*

data class TradingOrder(
    val id: UUID = UUID.randomUUID(),
    val traderId: UUID,
    val signal: TradingAlgorithm.Output,
    val atPrice: Double,
    val createdAt: Instant = Instant.now(),
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is TradingOrder) return false
        return id == other.id
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }
}