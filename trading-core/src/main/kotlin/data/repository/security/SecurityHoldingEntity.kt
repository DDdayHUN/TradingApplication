package data.repository.security

import data.repository.trader.TraderEntity
import domain.market.security.SecurityHolding
import jakarta.persistence.*
import java.time.Instant
import java.util.*

@Entity
@Table(name = "app_security_holding")
class SecurityHoldingEntity(
    @Id
    @Column(name = "id", nullable = false, updatable = false)
    var id: UUID,

    @Column(name = "time_stamp", nullable = false, updatable = false)
    var timestamp: Instant,

    @Column(name = "entry_price", nullable = false)
    var entryPrice: Double,

    @Column(name = "amount", nullable = false)
    var amount: Int,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "trader_id", nullable = false)
    var trader: TraderEntity
)

fun SecurityHolding.toEntity(trader: TraderEntity): SecurityHoldingEntity {
    return SecurityHoldingEntity(
        id = id,
        timestamp = timestamp,
        entryPrice = entryPrice,
        amount = amount,
        trader = trader
    )
}

fun SecurityHoldingEntity.toDomain(): SecurityHolding {
    return SecurityHolding(
        id = id,
        timestamp = timestamp,
        entryPrice = entryPrice,
        amount = amount
    )
}