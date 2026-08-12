package data.repository.user

import data.repository.portfolio.PortfolioEntity
import domain.User
import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.OneToOne
import jakarta.persistence.Table
import java.util.UUID

@Entity
@Table(name = "app_user")
class UserEntity (
    @Id
    var id: UUID
) {
    @OneToOne(mappedBy = "user", cascade = [CascadeType.ALL], orphanRemoval = true)
    lateinit var portfolio: PortfolioEntity

    fun attachPortfolio(portfolio: PortfolioEntity) {
        this.portfolio = portfolio
        portfolio.user = this
    }
}

fun User.toEntity(user: User): UserEntity {
    return UserEntity(
        id = user.id
    )
}

fun UserEntity.toDomain(): User {
    return User(
        id = id,
    )
}