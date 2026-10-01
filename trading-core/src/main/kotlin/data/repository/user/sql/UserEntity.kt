package data.repository.user.sql

import data.repository.portfolio.sql.PortfolioEntity
import domain.User
import jakarta.persistence.*
import java.util.*

@Entity
@Table(name = "app_user")
class UserEntity (
    @Id
    var id: UUID,

    @Column(name = "user_name", nullable = false)
    var userName: String
) {
    @OneToOne(mappedBy = "user", cascade = [CascadeType.ALL], orphanRemoval = true)
    var portfolio: PortfolioEntity?= null
}

fun User.toEntity(): UserEntity {
    return UserEntity(
        id = this.id,
        userName = this.userName
    )
}

fun UserEntity.toDomain(): User {
    return User(
        id = this.id,
        userName = this.userName,
    )
}