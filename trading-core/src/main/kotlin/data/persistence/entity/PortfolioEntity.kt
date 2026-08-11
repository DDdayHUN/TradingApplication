package data.persistence.entity

import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import jakarta.persistence.OneToOne
import java.util.UUID

@Entity
@Table(name = "app_portfolio")
class PortfolioEntity(

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    var user: UserEntity,

    @Column(name = "available_cash", nullable = false)
    var availableCash: Double = 0.0
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