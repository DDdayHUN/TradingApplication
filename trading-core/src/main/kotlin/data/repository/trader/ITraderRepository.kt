package data.repository.trader

import data.repository.trader.sql.TraderEntity
import java.util.UUID

interface ITraderRepository {
    suspend fun save(trader: TraderEntity): Result<TraderEntity>
    suspend fun saveAll(traders: Iterable<TraderEntity>): Result<List<TraderEntity>>
    suspend fun getById(traderId: UUID): Result<TraderEntity>
    suspend fun getAllById(traderIds: Iterable<UUID>): Result<List<TraderEntity>>
    suspend fun deleteById(traderId: UUID): Result<Unit>
    suspend fun deleteAllById(traderIds: Iterable<UUID>): Result<Unit>
    suspend fun deleteAll(): Result<Unit>
}