package data.repository.trader

import domain.algorithm.ITradingAlgorithm
import domain.market.security.SecurityHolding
import domain.market.security.SecurityIdentifier
import domain.trader.Trader
import java.util.UUID

data class TraderDto(
    val uuid: UUID,
    val securityIdentifier: SecurityIdentifier,
    val capital: Double,
    val holdings: List<SecurityHolding>,
    val algorithm: ITradingAlgorithm
) {
    fun toDomain(): Trader {
        return Trader(
            uuid,
            securityIdentifier,
            holdings.toMutableList(),
            capital,
            algorithm
        )
    }
}
