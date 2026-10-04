package api.dto

import application.service.user.User
import java.util.*

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