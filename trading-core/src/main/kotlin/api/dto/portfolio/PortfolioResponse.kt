package api.dto.portfolio

import api.dto.trader.TraderResponse
import api.dto.user.UserResponse

data class PortfolioResponse(
    val user: UserResponse,
    val availableCash: Double,
    val traders: List<TraderResponse>
)
