package data.repository.order

import application.service.broker.BrokerOrderRequest
import data.repository.security.toDomain
import data.repository.trader.TraderEntity
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

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    var status: OrderStatus,

    @Column(name = "filled_quantity", nullable = false)
    var filledQuantity: Double = 0.0,

    @Column(name = "average_fill_price")
    var averageFillPrice: Double? = null,

    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: Instant = Instant.now(),
)

enum class OrderAction {
    BUY,
    SELL
}

enum class OrderStatus {
    PENDING,
    SUBMITTED,
    PARTIALLY_FILLED,
    FILLED,
    CANCELLED,
    REJECTED
}

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

fun TradingOrder.toEntity(trader: TraderEntity, ibkrOrderId: Int): OrderEntity {
    val action: OrderAction
    val quantity: Double

    when {
        buy != null && sell == null -> {
            action = OrderAction.BUY
            quantity = buy.amount.toDouble()
        }

        sell != null && buy == null -> {
            action = OrderAction.SELL
            quantity = sell.batches
                .sumOf { (_, amount) -> amount }
                .toDouble()
        }

        else -> {
            throw IllegalArgumentException(
                "TradingOrder must contain exactly one action: BUY or SELL"
            )
        }
    }

    return OrderEntity(
        id = orderId,
        ibkrOrderId = ibkrOrderId,
        trader = trader,
        action = action,
        quantity = quantity,
        status = OrderStatus.PENDING,
        filledQuantity = 0.0,
        averageFillPrice = null,
        createdAt = createdAt,
    )
}

