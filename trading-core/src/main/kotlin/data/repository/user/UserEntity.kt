package data.repository.user

import data.repository.portfolio.PortfolioEntity
import domain.User
import jakarta.persistence.CascadeType
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.OneToMany
import jakarta.persistence.OneToOne
import jakarta.persistence.Table
import java.util.UUID

@Entity
@Table(name = "app_user")
class UserEntity (
    @Id
    var id: UUID
) {
    @OneToMany(mappedBy = "user", cascade = [CascadeType.ALL], orphanRemoval = true)
    var portfolios: MutableSet<PortfolioEntity> = mutableSetOf()
}

fun User.toEntity(): UserEntity {
    return UserEntity(
        id = this.id
    )
}

fun UserEntity.toDomain(): User {
    return User(
        id = this.id,
    )
}