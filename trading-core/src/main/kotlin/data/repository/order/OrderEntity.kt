package data.repository.order

import application.service.broker.BrokerOrderRequest
import data.repository.portfolio.PortfolioEntity
import data.repository.security.toDomain
import data.repository.trader.TraderEntity
import domain.order.Order
import domain.order.OrderAction
import domain.order.OrderStatus
import domain.trader.TradingOrder
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.time.Instant
import java.util.UUID



@Entity
@Table(name = "app_order")
class OrderEntity(
    @Id
    @Column(name ="id", nullable = false, updatable = false)
    var id: UUID,

    @Column(name = "ibkr_order_id", nullable = false)
    var ibkrOrderId: Int,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "trader_id", nullable = false)
    var trader: TraderEntity,

    @Enumerated(EnumType.STRING)
    @Column(name = "action", nullable = false)
    var action: OrderAction,

    @Column(name = "quantity", nullable = false)
    var quantity: Double,

    @Column(name = "signal_price", nullable = false)
    var signalPrice: Double,

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    var status: OrderStatus,

    @Column(name = "filled_quantity", nullable = false)
    var filledQuantity: String = "",

    @Column(name = "average_fill_price")
    var averageFillPrice: Double? = null,

    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: Instant = Instant.now(),
)

fun TradingOrder.toBrokerOrder(): BrokerOrderRequest? {
    if(this.buy == null && this.sell == null) return null
    val quantity: Double
    val side: OrderAction

    if(this.buy != null){
        quantity = this.buy.amount.toDouble()
        side = OrderAction.BUY
    }else {
        quantity = this.sell!!.batches.sumOf {(_,amount) -> amount}.toDouble()
        side = OrderAction.SELL
    }
    return BrokerOrderRequest(
        ticker = this.securityIdentifier.tickerSymbol,
        currency = this.securityIdentifier.currency,
        quantity = quantity,
        side = side
    )
}

fun Order.toEntity(trader: TraderEntity): OrderEntity {
    return OrderEntity(
        id = id,
        ibkrOrderId = ibkrOrderId,
        trader = trader,
        action = action,
        quantity = quantity,
        signalPrice = signalPrice,
        status = status,
        filledQuantity = filledQuantity,
        averageFillPrice = averageFillPrice,
        createdAt = createdAt
    )
}

fun OrderEntity.toDomain(): Order {
    return Order(
        id = id,
        ibkrOrderId = ibkrOrderId,
        traderId = trader.id,
        action = action,
        quantity = quantity,
        signalPrice = signalPrice,
        status = status,
        filledQuantity = filledQuantity,
        averageFillPrice = averageFillPrice,
        createdAt = createdAt
    )
}

