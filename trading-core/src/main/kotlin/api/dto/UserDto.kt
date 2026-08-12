package api.dto

import java.util.UUID

data class CreateUserRequest(
    val availableCash: Double
)

data class UserResponse (
    val id: UUID,
)