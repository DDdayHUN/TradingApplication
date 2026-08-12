package data.repository.user

import api.dto.UserResponse
import domain.User
import org.springframework.stereotype.Component

@Component
class UserMapper {

    fun toDomain(entity: UserEntity) : User {
        return User(
            id = requireNotNull(entity.id),
        )
    }

    fun toEntity(user: User) : UserEntity {
        return UserEntity(
            id = requireNotNull(user.id)
        )
    }
}