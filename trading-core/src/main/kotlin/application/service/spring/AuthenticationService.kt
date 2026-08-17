package application.service.spring

import application.service.IAuthenticationService
import domain.User
import domain.interfaces.IUserRepository
import exception.api.AuthenticationException
import exception.api.UserAlreadyExistsException
import exception.api.UserNotFoundException
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

//===========================================================//
//===========================================================//

@Service
class AuthenticationService(
    private val userRepository: IUserRepository,
) : IAuthenticationService {
    //===========================================================//
    //===========================================================//
    // Public Method(s)

    @Transactional(readOnly = true)
    override suspend fun currentUser(): User {
        val auth = SecurityContextHolder.getContext().authentication
            ?: throw AuthenticationException("No authentication present")

        val uuid: UUID =
            try { UUID.fromString(auth.name) }
            catch (e: IllegalArgumentException) {
                throw AuthenticationException("Invalid UUID", e)
            }

        return userRepository.getById(uuid).getOrThrow()
    }

    //===========================================================//

    @Transactional
    override suspend fun createUser(): User {
        val auth = SecurityContextHolder.getContext().authentication
            ?: throw AuthenticationException("No authentication present")

        val uuid: UUID =
            try { UUID.fromString(auth.name) }
            catch (e: IllegalArgumentException) {
                throw AuthenticationException("Invalid UUID", e)
            }

        val query = userRepository.getById(uuid)

        if(query.isSuccess) {
            throw UserAlreadyExistsException(query.getOrThrow().id)
        }
        else {
            val user = User(
                id = uuid,
            )
            return userRepository.save(user).getOrThrow()
        }
    }
}