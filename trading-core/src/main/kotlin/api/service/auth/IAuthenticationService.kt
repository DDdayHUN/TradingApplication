package api.service.auth

import application.service.user.User
import java.util.*

interface IAuthenticationService {
    suspend fun currentUser(): UUID
    suspend fun createUser(): User
}