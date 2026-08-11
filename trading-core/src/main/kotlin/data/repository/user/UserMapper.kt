package data.repository.user

import api.dto.UserResponse
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