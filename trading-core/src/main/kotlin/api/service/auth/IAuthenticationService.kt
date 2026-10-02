package api.service.auth

import application.model.User

interface IAuthenticationService {
    suspend fun currentUser(): User
    suspend fun createUser(): User
}