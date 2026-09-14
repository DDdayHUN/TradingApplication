package infrastructure.broker

data class IbkrHistoricalBar(
    val timestamp: String,
    val price: Double
)

data class IbkrAccountSummary(
    val availableCapital: Double,
    val netLiquidation: Double
)