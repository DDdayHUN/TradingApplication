package data.repository.order.sql

import data.repository.trader.sql.TraderEntity
import application.service.broker.InteractiveBrokersOrder
import application.service.broker.InteractiveBrokersOrder.Action
import application.service.broker.InteractiveBrokersOrder.Status
import data.repository.security.SecurityIdentifierEntity
import data.repository.security.toDomain
import data.repository.security.toEntity
import domain.trader.SellHolding
import jakarta.persistence.*
import java.time.Instant
import java.util.*
import kotlin.collections.map

@Entity
@Table(name = "app_order")
class InteractiveBrokersOrderEntity(
    @Id
    @Column(name ="id", nullable = false, updatable = false)
    var id: UUID,

    @Column(name = "ibkr_order_id", nullable = false, updatable = false)
    var ibkrOrderId: Int,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "trader_id", nullable = false, updatable = false)
    var trader: TraderEntity,

    @Embedded
    @AttributeOverrides(
        AttributeOverride(
            name = "isin",
            column = Column(
                name = "security_isin",
                nullable = false
            )
        ),
        AttributeOverride(
            name = "tickerSymbol",
            column = Column(
                name = "security_ticker",
                nullable = false
            )
        ),
        AttributeOverride(
            name = "currency",
            column = Column(
                name = "security_currency",
                nullable = false
            )
        )
    )
    var securityIdentifier: SecurityIdentifierEntity,

    @Enumerated(EnumType.STRING)
    @Column(name = "action", nullable = false, updatable = false)
    var action: Action,

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
    var sellAllocations: MutableSet<SellHoldingEntity> = mutableSetOf(),

    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: Instant = Instant.now(),
)

fun InteractiveBrokersOrder.toEntity(trader: TraderEntity): InteractiveBrokersOrderEntity {
    val action = when (signal){
        is InteractiveBrokersOrder.Signal.Buy -> Action.BUY
        is InteractiveBrokersOrder.Signal.Sell -> Action.SELL
    }
    val entity = InteractiveBrokersOrderEntity(
        id = id,
        ibkrOrderId = brokerOrderId,
        trader = trader,
        action = action,
        quantity = quantity,
        signalPrice = signalPrice,
        status = status,
        filledQuantity = filledQuantity,
        averageFillPrice = averageFillPrice,
        createdAt = createdAt,
        securityIdentifier = securityIdentifier.toEntity()
    )
    if(signal is InteractiveBrokersOrder.Signal.Sell){
        entity.sellAllocations =
            signal.allocations.map { (holding, amount) ->
                SellHoldingEntity(
                    order = entity,
                    holdingId = holding,
                    amount = amount
                )
            }.toMutableSet()
    }
    return entity
}

fun InteractiveBrokersOrderEntity.toInteractiveBrokersOrder(): InteractiveBrokersOrder {
    val signal = when (action) {
        Action.BUY -> {
            InteractiveBrokersOrder.Signal.Buy(amount = quantity.toInt())
        }
        Action.SELL -> {
            InteractiveBrokersOrder.Signal.Sell(
                allocations = sellAllocations.map {
                    SellHolding(
                        id = it.holdingId,
                        amount = it.amount
                    )
                }.toSet()
            )
        }
    }

    return InteractiveBrokersOrder(
        id = id,
        brokerOrderId = ibkrOrderId,
        traderId = trader.id,
        signal = signal,
        action = action,
        signalPrice = signalPrice,
        status = status,
        filledQuantity = filledQuantity,
        averageFillPrice = averageFillPrice,
        createdAt = createdAt,
        securityIdentifier = securityIdentifier.toDomain()
    )
}

