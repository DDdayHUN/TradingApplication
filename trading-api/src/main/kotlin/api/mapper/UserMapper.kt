package api.mapper

import api.dto.user.CreateUserRequest
import api.dto.user.UserResponse
import api.entity.UserEntity
import org.springframework.stereotype.Component

@Component
class UserMapper {

    fun toEntity(request: CreateUserRequest): UserEntity{
        return UserEntity(
            keycloakSub = request.keycloakSub
        )
    }

    fun toResponse(entity: UserEntity): UserResponse {
        return UserResponse(
            id = requireNotNull(entity.id),
            keycloakSub = entity.keycloakSub
        )
    }
}