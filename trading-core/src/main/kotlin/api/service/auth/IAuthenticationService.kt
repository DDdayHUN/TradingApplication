package api.service.auth

import java.util.UUID

interface IAuthenticationService {
    suspend fun currentUser(): UUID
    suspend fun createUser(): UUID
}