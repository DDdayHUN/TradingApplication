package data.infrastructure.broker.ibkr.sql.order

import data.infrastructure.broker.ibkr.InteractiveBrokersOrder
import data.infrastructure.broker.ibkr.InteractiveBrokersOrder.Status
import data.repository.order.sql.TradingOrderEntity
import data.repository.order.sql.toDomain
import jakarta.persistence.*

@Entity
@Table(name = "app_infrastructure_ibkr_order")
class InteractiveBrokersOrderEntity(
    @Id
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id", nullable = false, updatable = false)
    var tradingOrder: TradingOrderEntity,

    @Column(name = "ibkr_order_id", nullable = false, updatable = false)
    var ibkrOrderId: Int,

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    var status: Status,

    @Column(name = "filled_quantity", nullable = false)
    var filledQuantity: String = "",

    @Column(name = "average_fill_price")
    var averageFillPrice: Double? = null,
)

fun InteractiveBrokersOrder.toEntity(tradingOrder: TradingOrderEntity): InteractiveBrokersOrderEntity {
    return InteractiveBrokersOrderEntity(
        tradingOrder = tradingOrder,
        ibkrOrderId = brokerOrderId,
        status = status,
        filledQuantity = filledQuantity,
        averageFillPrice = averageFillPrice
    )
}