package data.repository.portfolio.sql

import data.repository.trader.sql.TraderEntity
import data.repository.trader.sql.toDomain
import data.repository.trader.sql.toEntity
import data.repository.trader.sql.update
import data.repository.user.sql.UserEntity
import domain.Portfolio
import jakarta.persistence.*
import java.util.*

@Entity
@Table(name = "app_portfolio")
class PortfolioEntity(
    @Id
    var id: UUID,
) {
    @OneToOne(mappedBy = "portfolio")
    var user: UserEntity? = null

    @OneToMany(mappedBy = "portfolio", fetch = FetchType.LAZY, orphanRemoval = true, cascade = [CascadeType.ALL])
    var traders: MutableSet<TraderEntity> = mutableSetOf()
}

fun Portfolio.toEntity(): PortfolioEntity {
    val entity = PortfolioEntity(
        id = id,
    )

    entity.traders = traders
        .map { trader -> trader.toEntity(entity) }
        .toMutableSet()

    return entity
}

fun PortfolioEntity.toDomain(): Portfolio {
    return Portfolio(
        id = id,
        traders = traders
            .map { trader -> trader.toDomain() }
            .toMutableSet()
    )
}

fun PortfolioEntity.update(portfolio: Portfolio) {
    require(this.id == portfolio.id) { "ID mismatch. ID1 {${this.id}} ID2 {${portfolio.id}}" }

    val domainTradersById = portfolio.traders.associateBy { it.id }

    // remove traders that no longer exist
    traders.removeIf { it.id !in domainTradersById }

    // update existing traders
    traders.forEach { existing ->
        domainTradersById[existing.id]?.let { domainTrader ->
            existing.update(domainTrader)
        }
    }

    // add new traders
    val existingIds = traders.map{ it.id }.toSet()

    portfolio.traders
        .filter { it.id !in existingIds }
        .forEach { domainTrader ->
            traders.add(domainTrader.toEntity(this))
        }
}