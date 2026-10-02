package data.repository.user.sql

import data.repository.portfolio.sql.PortfolioEntity
import application.model.User
import data.repository.portfolio.sql.toDomain
import data.repository.portfolio.sql.toEntity
import jakarta.persistence.*
import java.util.*

@Entity
@Table(name = "app_user")
class UserEntity (
    @Id
    var id: UUID,

    @Column(name = "user_name", nullable = false)
    var userName: String,

    @OneToOne(fetch = FetchType.LAZY, cascade = [CascadeType.ALL], orphanRemoval = true)
    @JoinColumn(name = "portfolio_id", referencedColumnName = "id", unique = true)
    var portfolio: PortfolioEntity
)

fun User.toEntity(): UserEntity {
    return UserEntity(
        id = id,
        userName = userName,
        portfolio = portfolios.elementAt(0).toEntity()
    )
}

fun UserEntity.toDomain(): User {
    return User(
        id = this.id,
        userName = this.userName,
        portfolios = setOf(portfolio.toDomain())
    )
}