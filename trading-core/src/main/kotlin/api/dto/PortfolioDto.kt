package api.dto

import domain.Portfolio
import java.util.*

//===========================================================//
//===========================================================//
//===========================================================//

data class PortfolioResponse(
    val id: UUID,
    val availableCapital: Double,
    val traders: List<TraderResponse>
)

//===========================================================//

fun Portfolio.toResponse(availableCapital: Double): PortfolioResponse {
    return PortfolioResponse(
        id = id,
        availableCapital = availableCapital,
        traders = traders.map { trader ->
            trader.toResponse()
        }
    )
}