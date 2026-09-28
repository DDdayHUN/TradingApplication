package infrastructure.broker

data class IbkrHistoricalBar(
    val timestamp: String,
    val price: Double
)

data class AccountSummary(
    val availableCapital: Double,
    val netLiquidation: Double
)