package application.service

import domain.User

interface IAuthenticationService {
    suspend fun currentUser(): User
    suspend fun createUser(): User
}