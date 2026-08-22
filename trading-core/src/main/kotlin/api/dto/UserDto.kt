package api.dto

import domain.User
import java.util.UUID

//===========================================================//
//===========================================================//

data class UserResponse (
    val id: UUID,
    val userName: String
)

//===========================================================//

fun User.toResponse(): UserResponse {
    return UserResponse(
        id = id,
        userName = userName
    )
}