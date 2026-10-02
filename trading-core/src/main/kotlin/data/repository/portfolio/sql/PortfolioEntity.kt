package data.repository.portfolio.sql

import data.repository.trader.sql.TraderEntity
import data.repository.trader.sql.toDomain
import data.repository.trader.sql.toEntity
import data.repository.user.sql.UserEntity
import domain.Portfolio
import jakarta.persistence.*
import jdk.internal.util.StaticProperty.userName
import java.util.*

@Entity
@Table(name = "app_portfolio")
class PortfolioEntity(
    @Id
    var id: UUID,
) {
    // TODO : Ezt majd átbeszélni, hogyan lehetne szebben, cuz scalingbe ez szar XD
    @OneToOne(mappedBy = "portfolio", fetch = FetchType.LAZY)
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