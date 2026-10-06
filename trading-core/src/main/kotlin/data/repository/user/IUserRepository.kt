package data.repository.user

import data.repository.user.sql.UserEntity
import java.util.UUID

interface IUserRepository {
    suspend fun save(user: UserEntity): Result<UserEntity>
    suspend fun saveAll(users: Iterable<UserEntity>): Result<List<UserEntity>>
    suspend fun getById(userId: UUID): Result<UserEntity>
    suspend fun getAllById(userIds: Iterable<UUID>): Result<List<UserEntity>>
    suspend fun deleteById(userId: UUID): Result<Unit>
    suspend fun deleteAllById(userIds: Iterable<UUID>): Result<Unit>
    suspend fun deleteAll(): Result<Unit>
}