package api.dto

import domain.Portfolio
import java.util.*

//===========================================================//
//===========================================================//
//===========================================================//

data class PortfolioResponse(
    val id: UUID,
    val availableCapital: Double,
    val allocatedCapital: Double,
    val traders: List<TraderResponse>
)

//===========================================================//

fun Portfolio.toResponse(): PortfolioResponse {
    return PortfolioResponse(
        id = id,
        availableCapital = availableCapital,
        allocatedCapital = allocatedCapital,
        traders = traders.map { trader -> trader.toResponse() }
    )
}