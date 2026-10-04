package data.repository.trader.sql

import data.repository.g_GSON
import data.repository.portfolio.sql.PortfolioEntity
import data.repository.security.SecurityHoldingEntity
import data.repository.security.SecurityIdentifierEntity
import data.repository.security.toDomain
import data.repository.security.toEntity
import domain.algorithm.ITradingAlgorithm
import domain.trader.Trader
import jakarta.persistence.*
import org.hibernate.annotations.ColumnTransformer
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import java.util.*

@Entity
@Table(name = "app_trader")
class TraderEntity(

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    var id: UUID,

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

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "portfolio_id", nullable = false)
    var portfolio: PortfolioEntity,

    @Column(name = "capital", nullable = false)
    var capital: Double,

    @Column(name = "algorithm_type", nullable = false)
    var algorithmType: String,

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "algorithm_state", nullable = false, columnDefinition = "jsonb")
    @ColumnTransformer(read = "cast(algorithm_state as text)", write = "cast(? as jsonb)")
    var algorithmState: String
) {
    @OneToMany(mappedBy = "trader", fetch = FetchType.LAZY, orphanRemoval = true, cascade = [CascadeType.ALL])
    var holdings: MutableSet<SecurityHoldingEntity> = mutableSetOf()
}

fun Trader.toEntity(portfolio: PortfolioEntity): TraderEntity {
    val entity = TraderEntity(
        id = id,
        securityIdentifier = securityIdentifier.toEntity(),
        portfolio = portfolio,
        capital = availableCapital,
        algorithmType = ITradingAlgorithm.typeTagOf(algorithm).getOrThrow(),
        algorithmState = g_GSON.toJson(
            algorithm,
            ITradingAlgorithm::class.java
        )
    )

    entity.holdings.addAll(holdings.map { holding ->
        holding.toEntity(entity)
    })

    return entity
}

fun TraderEntity.toDomain(): Trader {
    val algorithm = g_GSON.fromJson(
        algorithmState,
        ITradingAlgorithm::class.java
    )

    return Trader(
        id = id,
        securityIdentifier = securityIdentifier.toDomain(),
        holdings = holdings
            .map { holding ->
                holding.toDomain()
            }.toMutableSet(),
        allocatedCapital = capital,
        algorithm = algorithm
    )
}

fun TraderEntity.update(trader: Trader) {
    require(this.id == trader.id) { "Id mismatch: Entity: ${this.id} Trader: ${trader.id}" }

    securityIdentifier = trader.securityIdentifier.toEntity()
    capital = trader.availableCapital

    algorithmType = ITradingAlgorithm.typeTagOf(trader.algorithm).getOrThrow()
    algorithmState = g_GSON.toJson(
        trader.algorithm,
        ITradingAlgorithm::class.java
    )

    val domainHoldings = trader.holdings.associateBy { holding -> holding.id }

    // remove
    holdings.removeIf { holding -> holding.id !in domainHoldings }

    // update
    holdings.forEach { holding ->
        domainHoldings[holding.id]?.let { domainHolding ->
            holding.entryPrice = domainHolding.purchasePrice
            holding.amount = domainHolding.amount
            holding.timestamp = domainHolding.timestamp
        }
    }

    // add new
    val existingHoldingIds = holdings
        .map { it.id }
        .toSet()

    trader.holdings
        .filter { it.id !in existingHoldingIds }
        .forEach { holding ->
            holdings.add(holding.toEntity(this))
        }
}