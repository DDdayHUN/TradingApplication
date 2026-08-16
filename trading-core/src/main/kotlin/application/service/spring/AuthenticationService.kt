package application.service.spring

import application.service.IAuthenticationService
import domain.User
import domain.interfaces.IUserRepository
import exception.api.UserNotFoundException
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class AuthenticationService(
    private val userRepository: IUserRepository,
) : IAuthenticationService {
    override suspend fun currentUser(): User {
        val auth = SecurityContextHolder.getContext().authentication
            ?: throw UserNotFoundException("No authentication present")

        val uuid: UUID =
            try { UUID.fromString(auth.name) }
            catch (e: IllegalArgumentException) {
                throw UserNotFoundException("Invalid UUID Generated", e)
            }

        return userRepository.getById(uuid)
            .getOrElse { throw UserNotFoundException("User not found") }
    }

    override suspend fun createUser(): Result<User> {
        return Result.failure(UserNotFoundException("User not found"))
    }
}