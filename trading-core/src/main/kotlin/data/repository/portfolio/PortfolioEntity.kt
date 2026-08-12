package data.repository.portfolio

import data.repository.trader.TraderEntity
import data.repository.trader.toDomain
import data.repository.trader.toEntity
import data.repository.user.UserEntity
import domain.Portfolio
import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.OneToMany
import jakarta.persistence.OneToOne
import jakarta.persistence.Table
import java.util.UUID

@Entity
@Table(name = "app_portfolio")
class PortfolioEntity(

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    var user: UserEntity,

    @Column(name = "available_cash", nullable = false)
    var capital: Double
) {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    var id: UUID? = null

    @OneToMany(mappedBy = "portfolio", fetch = FetchType.LAZY, orphanRemoval = true, cascade = [CascadeType.ALL])
    var traders: MutableList<TraderEntity> = mutableListOf()

    fun addTrader(trader: TraderEntity){
        trader.portfolio = this
        if(!traders.contains(trader)) traders.add(trader)
    }

    fun removeTrader(trader: TraderEntity){
        traders.remove(trader)
    }
}

fun Portfolio.toEntity(user: UserEntity): PortfolioEntity {
    val entity = PortfolioEntity(
        user = user,
        capital = capital
    )

    traders.forEach {trader ->
        entity.addTrader(
            trader.toEntity(entity)
        )
    }

    return entity
}

fun PortfolioEntity.toDomain(): Portfolio {
    return Portfolio(
        userId = user.id,
        traders = traders
            .map { trader ->
                trader.toDomain()
            }
            .toMutableList(),
        capital = capital
    )
}