package data.repository.security

import data.infrastructure.broker.ibkr.order.sql.InteractiveBrokersOrderEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.util.UUID

@Entity
@Table(name = "app_order_sell_holdings")
class SellSecurityHoldingEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    var id: UUID? = null,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    var order: InteractiveBrokersOrderEntity,

    @Column(name = "holding_id", nullable = false)
    var holdingId : UUID,

    @Column(name = "amount", nullable = false)
    var amount: Int
)

