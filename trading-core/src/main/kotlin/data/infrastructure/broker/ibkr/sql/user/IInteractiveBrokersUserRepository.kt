package data.infrastructure.broker.ibkr.sql.user

import java.util.UUID

interface IInteractiveBrokersUserRepository {
    suspend fun save(user: InteractiveBrokersUserEntity): Result<InteractiveBrokersUserEntity>
    suspend fun saveAll(users: Iterable<InteractiveBrokersUserEntity>): Result<List<InteractiveBrokersUserEntity>>

    suspend fun getById(userId: UUID): Result<InteractiveBrokersUserEntity>
    suspend fun getAllById(userIds: Iterable<UUID>): Result<List<InteractiveBrokersUserEntity>>

    suspend fun deleteById(userId: UUID): Result<Unit>
    suspend fun deleteAllById(userIds: Iterable<UUID>): Result<Unit>
    suspend fun deleteAll(): Result<Unit>

    suspend fun fetchNextId(): Result<Int>
}