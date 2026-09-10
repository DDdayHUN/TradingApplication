package data.repository.order.sql

import application.service.broker.BrokerOrderRequest
import data.repository.security.SecurityIdentifierEntity
import data.repository.security.toDomain
import data.repository.trader.TraderEntity
import domain.order.Order
import domain.order.Order.OrderAction
import domain.order.Order.Status
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

    @Column(name = "ibkr_order_id")
    var ibkrOrderId: Int?,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "trader_id", nullable = false)
    var trader: TraderEntity,

    @Embedded
    @AttributeOverrides(
        AttributeOverride(
            name = "isin",
            column = Column(name = "security_isin", nullable = false)
        ),
        AttributeOverride(
            name = "tickerSymbol",
            column = Column(name = "security_ticker", nullable = false)
        ),
        AttributeOverride(
            name = "currency",
            column = Column(name = "security_currency", nullable = false)
        )
    )
    var securityIdentifier: SecurityIdentifierEntity,

    @Enumerated(EnumType.STRING)
    @Column(name = "action", nullable = false)
    var action: OrderAction,

    @Column(name = "quantity", nullable = false)
    var quantity: Double,

    @Column(name = "signal_price", nullable = false)
    var signalPrice: Double,

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    var status: Status,

    @Column(name = "filled_quantity", nullable = false)
    var filledQuantity: String = "",

    @Column(name = "average_fill_price")
    var averageFillPrice: Double? = null,

    @OneToMany(mappedBy = "order", fetch = FetchType.LAZY, cascade = [CascadeType.ALL], orphanRemoval = true)
    var sellAllocations: MutableList<SellAllocationEntity> = mutableListOf(),

    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: Instant = Instant.now(),
)

fun Order.toBrokerOrder(): BrokerOrderRequest {
    val side = when (signal) {
        is Order.Signal.Buy -> OrderAction.BUY
        is Order.Signal.Sell -> OrderAction.SELL
    }

    return BrokerOrderRequest(
        ticker = securityIdentifier.tickerSymbol,
        currency = securityIdentifier.currency,
        quantity = quantity,
        side = side
    )
}

fun Order.toEntity(trader: TraderEntity): OrderEntity {

    val action = when (signal){
        is Order.Signal.Buy -> OrderAction.BUY
        is Order.Signal.Sell -> OrderAction.SELL
    }

    val entity = OrderEntity(
        id = id,
        ibkrOrderId = ibkrOrderId,
        trader = trader,
        securityIdentifier = trader.securityIdentifier,
        action = action,
        quantity = quantity,
        signalPrice = signalPrice,
        status = status,
        filledQuantity = filledQuantity,
        averageFillPrice = averageFillPrice,
        createdAt = createdAt
    )
    if(signal is Order.Signal.Sell){
        entity.sellAllocations =
            signal.allocations.map { (holding, amount) ->
                SellAllocationEntity(
                    order = entity,
                    holdingId = holding,
                    amount = amount
                )
            }.toMutableList()
    }
    return entity
}

fun OrderEntity.toDomain(): Order {
    val signal = when (action) {

        OrderAction.BUY -> {
            Order.Signal.Buy(
                amount = quantity.toInt()
            )
        }

        OrderAction.SELL -> {
            Order.Signal.Sell(
                allocations = sellAllocations.map {
                    SellAllocation(
                        holdingId = it.holdingId,
                        amount = it.amount
                    )
                }
            )
        }
    }

    return Order(
        id = id,
        ibkrOrderId = ibkrOrderId,
        traderId = trader.id,
        securityIdentifier = securityIdentifier.toDomain(),
        signal = signal,
        signalPrice = signalPrice,
        status = status,
        filledQuantity = filledQuantity,
        averageFillPrice = averageFillPrice,
        createdAt = createdAt
    )
}

