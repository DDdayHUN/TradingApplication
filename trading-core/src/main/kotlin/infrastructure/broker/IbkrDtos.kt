package infrastructure.broker

import kotlinx.coroutines.CompletableDeferred
import java.util.UUID

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

data class AccountSummaryRequest(
    var availableCapital: Double?=null,
    var accountLiquidation: Double?=null,
    var result: CompletableDeferred<IbkrAccountSummary>
)