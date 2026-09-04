package data.repository.order.sql

import application.service.broker.BrokerOrderRequest
import data.repository.trader.TraderEntity
import domain.order.Order
import domain.order.OrderAction
import domain.order.OrderStatus
import domain.trader.TradingOrder
import infrastructure.broker.SellAllocation
import jakarta.persistence.*
import java.time.Instant
import java.util.*


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

    @OneToMany(mappedBy = "order", fetch = FetchType.LAZY, cascade = [CascadeType.ALL], orphanRemoval = true)
    var sellAllocations: MutableList<SellAllocationEntity> = mutableListOf(),

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
    val entity = OrderEntity(
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

    entity.sellAllocations = sellAllocations.map { allocation ->
        SellAllocationEntity(
            order = entity,
            holdingId = allocation.holdingId,
            amount = allocation.amount
        )
    }.toMutableList()

    return entity
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

        sellAllocations = sellAllocations.map { holding ->
            SellAllocation(
                holdingId = holding.holdingId,
                amount = holding.amount,
            )
        },
        createdAt = createdAt
    )
}

