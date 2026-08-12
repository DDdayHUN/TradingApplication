package data.repository.trader

import com.google.gson.Gson
import data.repository.SecurityHoldingEntity
import data.repository.SecurityIdentifierEntity
import data.repository.portfolio.PortfolioEntity
import data.repository.toDomain
import data.repository.toEntity
import domain.algorithm.ITradingAlgorithm
import domain.trader.Trader
import jakarta.persistence.AttributeOverride
import jakarta.persistence.AttributeOverrides
import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Embedded
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import org.hibernate.annotations.ColumnTransformer
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import java.util.UUID

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
    var holdings: MutableList<SecurityHoldingEntity> = mutableListOf()

    //===========================================================//
    //===========================================================//
    // Public Method(s)

    fun addHolding(holding: SecurityHoldingEntity){
        holding.trader = this
        holdings.add(holding)
    }

    fun removeHolding(holding: SecurityHoldingEntity){
        holdings.remove(holding)
    }
}

fun Trader.toEntity(portfolio: PortfolioEntity, gson: Gson = Gson()): TraderEntity {
    val entity = TraderEntity(
        id = id,
        securityIdentifier = securityIdentifier.toEntity(),
        portfolio = portfolio,
        capital = capital,
        algorithmType = ITradingAlgorithm.typeTagOf(algorithm),
        algorithmState =  gson.toJson(
            algorithm,
            ITradingAlgorithm::class.java
        )
    )

    holdings.forEach {holding ->
        entity.addHolding(
            holding.toEntity(entity)
        )
    }

    return entity
}

fun TraderEntity.toDomain(gson: Gson = Gson()): Trader {
    val algorithm = gson.fromJson(
        algorithmState,
        ITradingAlgorithm::class.java
    )

    return Trader(
        id = id,
        securityIdentifier = securityIdentifier.toDomain(),
        holdings = holdings
            .map {holding ->
                holding.toDomain()
            }.toMutableList(),
        allocatedCapital = capital,
        algorithm = algorithm
    )
}