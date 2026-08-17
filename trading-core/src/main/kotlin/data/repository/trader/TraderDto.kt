package data.repository.trader

import domain.algorithm.ITradingAlgorithm
import domain.market.security.SecurityHolding
import domain.market.security.SecurityIdentifier
import domain.trader.Trader
import java.util.UUID

@Deprecated("FakeTraderDto")
internal data class TraderDto(
    val id: UUID,
    val securityIdentifier: SecurityIdentifier,
    val capital: Double,
    val holdings: Set<SecurityHolding>,
    val algorithm: ITradingAlgorithm
) {
    fun toDomain(): Trader {
        return Trader(
            id,
            securityIdentifier,
            holdings.toMutableSet(),
            capital,
            algorithm
        )
    }
}
