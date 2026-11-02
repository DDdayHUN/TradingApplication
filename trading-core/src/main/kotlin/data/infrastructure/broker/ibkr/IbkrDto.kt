package data.infrastructure.broker.ibkr

import kotlinx.coroutines.CompletableDeferred

data class IbkrPortfolioAccountSummary(
    val availableCapital: Double,
    val netLiquidation: Double
)

data class IbkrHistoricalBar(
    val timestamp: String,
    val price: Double
)

data class HistoricalDataRequest(
    val bars: MutableList<IbkrHistoricalBar> = mutableListOf(),
    val result: CompletableDeferred<List<IbkrHistoricalBar>>
)