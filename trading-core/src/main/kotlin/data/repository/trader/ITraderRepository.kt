package data.repository.trader

import domain.trader.Trader
import java.util.UUID

interface ITraderRepository {
    suspend fun save(trader: Trader): Result<Trader>
    suspend fun getById(traderId: UUID): Result<Trader>
    // TODO : suspend fun getAll(): Result<List<Trader>>
    suspend fun deleteById(traderId: UUID): Result<Unit>
    // TODO : suspend fun deleteAll(): Result<Unit>
}