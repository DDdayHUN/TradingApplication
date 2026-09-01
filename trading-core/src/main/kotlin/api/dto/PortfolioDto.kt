package api.dto

import domain.Portfolio
import java.util.*

//===========================================================//
//===========================================================//
//===========================================================//

data class PortfolioResponse(
    val id: UUID,
    val availableCapital: Double,
    val accountLiquidation: Double,
    val traders: List<TraderResponse>
)

//===========================================================//

fun Portfolio.toResponse(availableCapital: Double, liquidation: Double): PortfolioResponse {
    return PortfolioResponse(
        id = id,
        availableCapital = availableCapital,
        accountLiquidation = liquidation,
        traders = traders.map { trader ->
            trader.toResponse()
        }
    )
}