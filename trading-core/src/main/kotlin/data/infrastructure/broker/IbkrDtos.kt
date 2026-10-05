package data.infrastructure.broker

data class IbkrHistoricalBar(
    val timestamp: String,
    val price: Double
)

data class IbkrPortfolioAccountSummary(
    val availableCapital: Double,
    val netLiquidation: Double
)