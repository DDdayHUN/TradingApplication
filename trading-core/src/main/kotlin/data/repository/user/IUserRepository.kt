package data.repository.user

import data.repository.user.sql.UserEntity
import java.util.UUID

interface IUserRepository {
    suspend fun save(user: UserEntity): Result<UserEntity>
    suspend fun getById(userId: UUID): Result<UserEntity>
    suspend fun getAll(): Result<List<UserEntity>>
    suspend fun deleteById(userId: UUID): Result<Unit>
    suspend fun deleteAll(): Result<Unit>
}