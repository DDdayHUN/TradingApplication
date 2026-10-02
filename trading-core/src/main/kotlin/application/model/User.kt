package application.model

import domain.Portfolio
import java.util.UUID

data class User(
    val id: UUID,
    val userName: String,
    val portfolios: Set<Portfolio> // Egy elemű a Set jelen pillanatban.
) {
    data class AccountSummary(
        val availableCapital: Double,
        val netLiquidation: Double
    )
}