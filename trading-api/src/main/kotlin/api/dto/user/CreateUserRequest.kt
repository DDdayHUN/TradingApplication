package api.dto.user

data class CreateUserRequest(
    val keycloakSub: String,
    val availableCash: Double
)
