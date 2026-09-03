package infrastructure.broker

import java.util.*

data class IbkrHistoricalBar(
    val timestamp: String,
    val price: Double
)

data class SellAllocation (
    val holdingId : UUID,
    val amount: Int
)

data class IbkrAccountSummary(
    val availableCapital: Double,
    val netLiquidation: Double
)