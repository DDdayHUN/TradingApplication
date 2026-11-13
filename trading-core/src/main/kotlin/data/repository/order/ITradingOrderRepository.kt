package data.repository.order

import data.repository.order.sql.TradingOrderEntity
import java.util.UUID

interface ITradingOrderRepository {
    suspend fun save(order: TradingOrderEntity): Result<TradingOrderEntity>
    suspend fun saveAll(order: Iterable<TradingOrderEntity>): Result<List<TradingOrderEntity>>

    suspend fun getById(orderId: UUID): Result<TradingOrderEntity>
    suspend fun getAllById(orderIds: Iterable<UUID>): Result<List<TradingOrderEntity>>

    suspend fun deleteById(orderId: UUID): Result<Unit>
    suspend fun deleteAllById(orderIds: Iterable<UUID>): Result<Unit>
    suspend fun deleteAll(): Result<Unit>
}