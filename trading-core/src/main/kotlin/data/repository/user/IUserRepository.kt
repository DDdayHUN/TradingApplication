package data.repository.user

import data.repository.user.sql.UserEntity
import java.util.UUID

interface IUserRepository {
    suspend fun save(user: UserEntity): Result<UserEntity>
    suspend fun saveAll(users: Iterable<UserEntity>): Result<List<UserEntity>>

    suspend fun getById(userId: UUID): Result<UserEntity>
    suspend fun getAllById(userIds: Iterable<UUID>): Result<List<UserEntity>>
    // TODO [v2.0]: "This must be replaced in future releases, cuz this doesn't support large amounts of users"
    suspend fun getAllUserId(): Result<List<UUID>>

    suspend fun deleteById(userId: UUID): Result<Unit>
    suspend fun deleteAllById(userIds: Iterable<UUID>): Result<Unit>
    suspend fun deleteAll(): Result<Unit>
}