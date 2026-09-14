package data.repository.order.sql

import jakarta.persistence.*
import java.util.*

@Entity
@Table(name = "app_order_sell_allocation")
class SellHoldingEntity(

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
