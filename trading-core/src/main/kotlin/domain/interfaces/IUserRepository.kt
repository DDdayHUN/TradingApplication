package domain.interfaces

import domain.User
import java.util.UUID

interface IUserRepository {
    suspend fun save(user: User): Result<User>
    suspend fun getById(id: UUID): Result<User>
    suspend fun getAll(): Result<List<User>>
}