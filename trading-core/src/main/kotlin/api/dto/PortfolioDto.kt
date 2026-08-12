package api.dto

import java.util.UUID

data class PortfolioResponse(
    val id: UUID,
    val userId: UUID,
    val availableCash: Double,
    val traders: List<TraderResponse>
)

