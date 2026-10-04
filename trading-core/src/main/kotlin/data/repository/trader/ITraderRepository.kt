package data.repository.trader

import data.repository.trader.sql.TraderEntity
import java.util.UUID

interface ITraderRepository {
    suspend fun save(trader: TraderEntity): Result<TraderEntity>
    suspend fun getById(traderId: UUID): Result<TraderEntity>
    // suspend fun getAll(): Result<List<TraderEntity>>
    suspend fun deleteById(traderId: UUID): Result<Unit>
    // suspend fun deleteAll(): Result<Unit>
}