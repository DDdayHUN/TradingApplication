package application.model

import domain.Portfolio
import java.util.UUID

data class User(
    val id: UUID,
    val userName: String,
    val portfolio: Portfolio
) {
    data class AccountSummary(
        val availableCapital: Double,
        val netLiquidation: Double
    )
}