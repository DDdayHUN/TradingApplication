package data.repository.security

import data.repository.order.sql.TradingOrderEntity
import domain.trader.SellSecurityHolding
import jakarta.persistence.*
import org.springframework.data.jpa.domain.AbstractPersistable_.id
import java.util.*

@Entity
@Table(name = "app_order_sell_holdings")
class SellSecurityHoldingEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    var id: UUID? = null,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_fk", nullable = false, updatable = false)
    var order: TradingOrderEntity,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "holding_fk", nullable = false, updatable = false)
    var holding : SecurityHoldingEntity,

    @Column(name = "amount", nullable = false)
    var amount: Int
)

fun SellSecurityHolding.toEntity(order: TradingOrderEntity, holding: SecurityHoldingEntity): SellSecurityHoldingEntity {
    return SellSecurityHoldingEntity(
        order = order,
        holding = holding,
        amount = amount
    )
}

fun SellSecurityHoldingEntity.toDomain(): SellSecurityHolding {
    return SellSecurityHolding(
        securityHolding = holding.toDomain(),
        amount = amount
    )
}