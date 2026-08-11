package data.persistence.mapper

import api.dto.UserResponse
import data.persistence.entity.UserEntity
import org.springframework.stereotype.Component

@Component
class UserMapper {

    fun toResponse(entity: UserEntity): UserResponse {
        return UserResponse(
            id = requireNotNull(entity.id),
            keycloakSub = entity.keycloakSub
        )
    }
}