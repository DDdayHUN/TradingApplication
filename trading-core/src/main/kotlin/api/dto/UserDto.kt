package api.dto

import domain.User
import java.util.UUID

data class UserResponse (
    val id: UUID,
)

fun User.toResponse(): UserResponse {
    return UserResponse(
        id = id
    )
}