package application.service.user

import java.util.*

interface IUserService {
    suspend fun getById(userId: UUID): User
}