package api.dto

import domain.Portfolio
import java.util.UUID

data class PortfolioResponse(
    val id: UUID,
    val userId: UUID,
    val availableCash: Double,
    val traders: List<TraderResponse>
)

fun Portfolio.toResponse(): PortfolioResponse = PortfolioResponse(
    id = id,
    userId = userId,
    availableCash = availableCash,
    traders = traders.map { trader ->
        trader.toResponse()
    }
)