package api.service.auth

import java.util.*

interface IAuthenticationService {
    suspend fun currentUser(): UUID
    suspend fun createUser(): UUID
}