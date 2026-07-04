package data.repository.trader

import domain.assets.security.SecurityIdentifier
import domain.trader.Trader

sealed interface ITraderRepository {
    suspend fun save(trader: Trader)
    suspend fun getBySecurityIdentifier(securityIdentifier: SecurityIdentifier): Trader
    suspend fun getAll(): List<Trader>
}