package domain.trader

import domain.algorithm.TradingAlgorithm
import domain.market.security.SecurityIdentifier
import java.time.Instant
import java.util.*

data class TradingOrder(
    val orderId: UUID = UUID.randomUUID(),
    val traderId: UUID,
    val signal: TradingAlgorithm.Output,
    val atPrice: Double,
    val createdAt: Instant = Instant.now(),
)