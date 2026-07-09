package application.runner

import domain.algorithm.TradingAlgorithm
import domain.market.security.SecurityIdentifier
import domain.tax.Taxation
import kotlin.time.Instant

data class RunConfig(
    val algorithm: TradingAlgorithm.Type,
    val taxation: Taxation.Type,
    val identifier: SecurityIdentifier,
    val startCapital: Double,
    val startDate: Instant,
    val endDate: Instant,
    val evaluationWindowStepYears: Int
)