package api.dto.user

import java.util.UUID

data class UserResponse (
    val id: UUID,
    val keycloakSub: String
)