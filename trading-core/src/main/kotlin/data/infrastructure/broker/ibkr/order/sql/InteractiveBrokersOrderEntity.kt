package data.infrastructure.broker.ibkr.order.sql

import data.infrastructure.broker.ibkr.InteractiveBrokersOrder
import data.infrastructure.broker.ibkr.InteractiveBrokersOrder.Status
import data.repository.order.sql.TradingOrderEntity
import data.repository.order.sql.toDomain
import jakarta.persistence.*
import java.util.*

@Entity
@Table(name = "app_infrastructure_ibkr_order")
class InteractiveBrokersOrderEntity(
    @Id
    @Column(name = "id")
    var id: UUID,

    @Column(name = "ibkr_order_id", nullable = false, updatable = false)
    var ibkrOrderId: Int,

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "trading_order_fk", nullable = false, updatable = false)
    var tradingOrder: TradingOrderEntity,

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
        id = tradingOrder.id,
        ibkrOrderId = brokerOrderId,
        tradingOrder = tradingOrder,
        status = status,
        filledQuantity = filledQuantity,
        averageFillPrice = averageFillPrice
    )
}

fun InteractiveBrokersOrderEntity.toInteractiveBrokersOrder(): InteractiveBrokersOrder {
    return InteractiveBrokersOrder(
        tradingOrder = tradingOrder.toDomain(),
        brokerOrderId = ibkrOrderId,
        status = status,
        filledQuantity = filledQuantity,
        averageFillPrice = averageFillPrice,
    )
}