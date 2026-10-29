package application.service.user

import java.util.*

interface IUserService {
    suspend fun update(user: User): User
    suspend fun getById(userId: UUID): User

    // TODO [v2.0]: "This must be replaced in future releases, cuz this doesn't support large amounts of users"
    suspend fun getAllUserId(): List<UUID>
}