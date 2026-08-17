package api.dto

import domain.Portfolio
import java.util.UUID

//===========================================================//
//===========================================================//

data class CreatePortfolioRequest(
    val capital: Double
)

//===========================================================//

data class PortfolioResponse(
    val id: UUID,
    val capital: Double,
    val traders: List<TraderResponse>
)

//===========================================================//

fun Portfolio.toResponse(): PortfolioResponse {
    return PortfolioResponse(
        id = id,
        capital = capital,
        traders = traders.map { trader ->
            trader.toResponse()
        }
    )
}