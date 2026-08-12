package data.repository

import data.repository.trader.sql.TraderEntity
import domain.market.security.SecurityHolding
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.Table
import jakarta.persistence.ManyToOne
import java.util.UUID

@Entity
@Table(name = "app_security_holding")
class SecurityHoldingEntity(
    @Id
    @Column(name = "id", nullable = false, updatable = false)
    var id: UUID,

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
        entryPrice = entryPrice,
        amount = amount,
        trader = trader
    )
}

fun SecurityHoldingEntity.toDomain(): SecurityHolding {
    return SecurityHolding(
        id = id,
        entryPrice = entryPrice,
        amount = amount
    )
}