package api.dto

data class PortfolioResponse(
    val user: UserResponse,
    val availableCash: Double,
    val traders: List<TraderResponse>
)