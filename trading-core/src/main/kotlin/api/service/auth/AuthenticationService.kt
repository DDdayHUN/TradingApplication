package api.service.auth

import application.logging.logger
import data.repository.user.IUserRepository
import application.service.user.User
import data.repository.user.sql.toDomain
import data.repository.user.sql.toEntity
import domain.Portfolio
import exception.AuthenticationException
import exception.UserAlreadyExistsException
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class AuthenticationService(
    private val userRepository: IUserRepository
) : IAuthenticationService {
    //===========================================================//
    //===========================================================//
    // Private Field(s)

    private val m_Logger = logger<AuthenticationService>()

    //===========================================================//
    //===========================================================//
    // Public Method(s)

    override suspend fun currentUser(): UUID {
        val auth = SecurityContextHolder.getContext().authentication
            ?: throw AuthenticationException("No authentication present")

        return try {
            UUID.fromString(auth.name)
        } catch (e: IllegalArgumentException) {
            throw AuthenticationException("Invalid user UUID", e)
        }
    }

    //===========================================================//

    @Transactional
    override suspend fun createUser(): UUID {
        val auth = SecurityContextHolder.getContext().authentication
            ?: run {
                m_Logger.warn("User creation attempted without authentication")
                throw AuthenticationException("No authentication present")
            }

        val uuid: UUID =
            try { UUID.fromString(auth.name) }
            catch (e: IllegalArgumentException) {
                m_Logger.warn("Authentication contained invalid user UUID: {}", auth)
                throw AuthenticationException("Invalid UUID", e)
            }

        val jwtAuth = auth as? JwtAuthenticationToken
            ?: run {
                m_Logger.warn("User creation attempted without authentication type")
                throw AuthenticationException("Invalid JWT token")
            }

        val username = jwtAuth.token.getClaimAsString("preferred_username")
            ?: run {
                m_Logger.warn("JWT token missing preferred_username claim for user id={}", uuid)
                throw AuthenticationException("Username missing from token")
            }

        val query = userRepository.getById(uuid)

        if(query.isSuccess) {
            m_Logger.warn("User Already exists id={}", uuid)
            throw UserAlreadyExistsException(query.getOrThrow().id)
        }
        else {
            val newUser = User(
                id = uuid,
                userName = username,
                portfolio = Portfolio()
            )

            return userRepository.save(newUser.toEntity())
                .onSuccess { user ->
                    m_Logger.info(
                        "User created successfully id={} username={}",
                        user.id,
                        user.userName
                    )
                }.onFailure { user ->
                  m_Logger.error(
                      "Failed to create user id={}",
                      uuid, user
                  )
                }
                .getOrThrow()
                .toDomain()
                .id
        }
    }
}