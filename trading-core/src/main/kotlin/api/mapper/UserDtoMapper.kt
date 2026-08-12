package api.mapper

import api.dto.UserResponse
import domain.User
import org.springframework.stereotype.Component

@Component
class UserDtoMapper {

    fun toResponse(user: User): UserResponse {
        return UserResponse(
            id = user.id
        )
    }
}