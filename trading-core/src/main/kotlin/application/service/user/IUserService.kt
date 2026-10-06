package application.service.user

import java.util.*

interface IUserService {
    suspend fun update(user: User): User
    suspend fun getById(userId: UUID): User
}