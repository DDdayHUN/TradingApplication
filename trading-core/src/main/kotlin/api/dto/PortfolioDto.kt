package api.dto

import domain.Portfolio
import java.util.*

//===========================================================//
//===========================================================//
//===========================================================//

data class PortfolioResponse(
    val id: UUID,
    val ibkrAvailableFunds: Double?,
    val ibkrNetLiquidation: Double?,
    val availableCapital: Double,
    val netLiquidation: Double,
    val traders: List<TraderResponse>
)

//===========================================================//

fun Portfolio.toResponse(ibkrAvailableFunds: Double? = null, ibkrNetLiquidation: Double? = null): PortfolioResponse {
    return PortfolioResponse(
        id = id,
        availableCapital = availableCapital,
        netLiquidation = netLiquidation,
        traders = traders.map { trader -> trader.toResponse() },
        ibkrAvailableFunds = ibkrAvailableFunds,
        ibkrNetLiquidation = ibkrNetLiquidation
    )
}