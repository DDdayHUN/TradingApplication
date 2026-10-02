package application.service.user

import application.model.User
import java.util.UUID

interface IUserService {
    suspend fun getById(userId: UUID): User
}