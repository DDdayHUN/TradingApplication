package data.repository.trader

import domain.trader.Trader
import java.util.UUID

interface ITraderRepository {
    suspend fun save(trader: Trader): Result<Trader>
    suspend fun delete(traderId: UUID): Result<Unit>
    suspend fun getById(traderId: UUID): Result<Trader>
}