package infrastructure.broker

import java.util.UUID

data class IbkrHistoricalBar(
    val date: String,
    val closingPrice: Double
)

data class SellAllocation (
    val holdingId : UUID,
    val amount: Int
)