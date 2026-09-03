package data.repository.order

import jakarta.persistence.*
import java.util.*

@Entity
@Table(name = "app_order_sell_allocation")
class SellAllocationEntity(

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    var id: UUID? = null,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    var order: OrderEntity,

    @Column(name = "holding_id", nullable = false)
    var holdingId : UUID,

    @Column(name = "amount", nullable = false)
    var amount: Int
)
