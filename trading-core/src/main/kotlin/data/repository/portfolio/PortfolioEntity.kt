package data.repository.portfolio

import data.repository.trader.TraderEntity
import data.repository.trader.toDomain
import data.repository.trader.toEntity
import data.repository.user.UserEntity
import domain.Portfolio
import jakarta.persistence.*
import java.util.*

@Entity
@Table(name = "app_portfolio")
class PortfolioEntity(

    @Id
    var id: UUID,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    var user: UserEntity,

    @Column(name = "capital", nullable = false)
    var capital: Double
) {

    @OneToMany(mappedBy = "portfolio", fetch = FetchType.LAZY, orphanRemoval = true, cascade = [CascadeType.ALL])
    var traders: MutableSet<TraderEntity> = mutableSetOf()

    fun addTrader(trader: TraderEntity){
        trader.portfolio = this
        if(!traders.contains(trader)) traders.add(trader)
    }
}

fun Portfolio.toEntity(user: UserEntity): PortfolioEntity {
    val entity = PortfolioEntity(
        id = id,
        user = user,
        capital = capital
    )

    traders.forEach { trader ->
        entity.addTrader(
            trader.toEntity(entity)
        )
    }

    return entity
}

fun PortfolioEntity.toDomain(): Portfolio {
    return Portfolio(
        id = id,
        traders = traders
            .map { trader ->
                trader.toDomain()
            }.toMutableSet(),
        capital = capital
    )
}