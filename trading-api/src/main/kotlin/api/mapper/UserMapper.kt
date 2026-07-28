package api.mapper

import api.dto.user.CreateUserRequest
import api.dto.user.UserResponse
import api.entity.User
import org.springframework.stereotype.Component

@Component
class UserMapper {

    fun toEntity(request: CreateUserRequest): User{
        return User(
            keycloakSub = request.keycloakSub
        )
    }

    fun toResponse(entity: User): UserResponse {
        return UserResponse(
            id = requireNotNull(entity.id),
            keycloakSub = entity.keycloakSub
        )
    }
}