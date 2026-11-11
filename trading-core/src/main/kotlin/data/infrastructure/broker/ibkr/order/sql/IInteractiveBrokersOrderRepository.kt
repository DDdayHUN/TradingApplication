package data.infrastructure.broker.ibkr.order.sql

import java.util.UUID

interface IInteractiveBrokersOrderRepository {
    suspend fun save(order: InteractiveBrokersOrderEntity): Result<InteractiveBrokersOrderEntity>
    suspend fun saveAll(order: Iterable<InteractiveBrokersOrderEntity>): Result<List<InteractiveBrokersOrderEntity>>

    suspend fun getById(orderId: UUID): Result<InteractiveBrokersOrderEntity>
    suspend fun getAllById(orderIds: Iterable<UUID>): Result<List<InteractiveBrokersOrderEntity>>

    suspend fun deleteById(orderId: UUID): Result<Unit>
    suspend fun deleteAllById(orderIds: Iterable<UUID>): Result<Unit>
    suspend fun deleteAll(): Result<Unit>

    suspend fun getAllByUserId(userId: UUID): Result<List<InteractiveBrokersOrderEntity>>
}