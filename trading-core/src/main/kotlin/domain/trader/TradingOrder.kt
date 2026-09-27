package domain.trader

import domain.algorithm.TradingAlgorithm
import domain.market.security.SecurityIdentifier
import java.time.Instant
import java.util.*

data class TradingOrder(
    val orderId: UUID = UUID.randomUUID(),
    val traderId: UUID,
    val securityIdentifier: SecurityIdentifier,
    val signal: TradingAlgorithm.Output,
    val atPrice: Double,
    val createdAt: Instant = Instant.now(),
) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is TradingOrder) return false
        return orderId == other.orderId
    }

    override fun hashCode(): Int {
        return orderId.hashCode()
    }
}