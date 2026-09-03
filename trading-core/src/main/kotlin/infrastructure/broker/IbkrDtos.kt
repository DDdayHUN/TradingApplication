package infrastructure.broker

import java.util.*

data class IbkrHistoricalBar(
    val date: String,
    val closingPrice: Double
)

data class SellAllocation (
    val holdingId : UUID,
    val amount: Int
)

data class IbkrAccountSummary(
    val availableCapital: Double,
    val netLiquidation: Double
)