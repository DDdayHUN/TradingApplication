package data.repository.trader

import domain.algorithm.TradingAlgorithm
import domain.assets.security.SecurityIdentifier
import domain.trader.Trader

interface ITraderRepository {
    suspend fun save(trader: Trader, algorithmType: TradingAlgorithm.Type)
    suspend fun getBySecurityIdentifier(securityIdentifier: SecurityIdentifier): Trader?
    suspend fun loadAll(): List<Trader>
}