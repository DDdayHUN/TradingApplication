package data.repository.order.sql

import data.repository.security.SecurityIdentifierEntity
import data.repository.security.SellSecurityHoldingEntity
import data.repository.security.toDomain
import data.repository.security.toEntity
import data.repository.trader.sql.TraderEntity
import domain.trader.TradingOrder
import domain.trader.TradingOrder.Signal
import jakarta.persistence.*
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "app_order")
class TradingOrderEntity(
    @Id
    @Column(name = "id")
    var id: UUID,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "trader_fk", nullable = false, updatable = false)
    var trader: TraderEntity,

    @Embedded
    @AttributeOverrides(
        AttributeOverride(
            name = "isin",
            column = Column(
                name = "security_isin",
                nullable = false,
                updatable = false
            )
        ),
        AttributeOverride(
            name = "tickerSymbol",
            column = Column(
                name = "security_ticker",
                nullable = false,
                updatable = false
            )
        ),
        AttributeOverride(
            name = "currency",
            column = Column(
                name = "security_currency",
                nullable = false,
                updatable = false
            )
        )
    )
    var securityIdentifier: SecurityIdentifierEntity,

    @Enumerated(EnumType.STRING)
    @Column(name = "action", nullable = false, updatable = false)
    var action: Action,

    // When Action is BUY
    @Column(name = "buy_amount")
    var buyAmount: Int? = null,

    @Column(name = "signal_price", nullable = false, updatable = false)
    var signalPrice: Double,

    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: Instant,
) {
    // When Action is SELL
    @OneToMany(mappedBy = "order", fetch = FetchType.LAZY, orphanRemoval = true, cascade = [CascadeType.ALL])
    var sellHoldings: MutableSet<SellSecurityHoldingEntity>? = null

    enum class Action {
        BUY,
        SELL
    }
}

fun TradingOrder.toEntity(trader: TraderEntity): TradingOrderEntity {
    val action = if(signal is Signal.Buy) TradingOrderEntity.Action.BUY else TradingOrderEntity.Action.SELL

    val quantity = when(signal) {
        is Signal.Buy -> signal.amount
        is Signal.Sell -> null
    }

    val entity = TradingOrderEntity(
        id = this.id,
        trader = trader,
        securityIdentifier = securityIdentifier.toEntity(),
        action = action,
        buyAmount = quantity,
        signalPrice = atPrice,
        createdAt = createdAt
    )

    entity.sellHoldings =
        if(signal is Signal.Sell) {
            signal.sellHoldings
                .map{
                    it.toEntity(
                    this.toEntity(trader),
                    it.securityHolding.toEntity(trader)
                    )
                }
                .toMutableSet()
        }
        else null

    return entity;
}

fun TradingOrderEntity.toDomain(): TradingOrder {
    val signal = when(action) {
        TradingOrderEntity.Action.BUY -> {
            Signal.Buy(buyAmount!!)
        }
        TradingOrderEntity.Action.SELL -> {
            Signal.Sell(sellHoldings!!.map { it.toDomain() }.toSet())
        }
    }

    return TradingOrder(
        id = this.id,
        traderId = trader.id,
        securityIdentifier = securityIdentifier.toDomain(),
        signal = signal,
        atPrice = signalPrice,
        createdAt = createdAt,
    )
}

