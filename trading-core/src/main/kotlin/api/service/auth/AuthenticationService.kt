package api.service.auth

import application.logging.logger
import data.repository.user.IUserRepository
import application.model.User
import data.repository.user.sql.toDomain
import data.repository.user.sql.toEntity
import domain.Portfolio
import exception.api.AuthenticationException
import exception.api.UserAlreadyExistsException
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

    private val logger = logger<AuthenticationService>()

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
    override suspend fun createUser(): User {
        val auth = SecurityContextHolder.getContext().authentication
            ?: run{
                logger.warn("User creation attempted without authentication")
                throw AuthenticationException("No authentication present")
            }

        val uuid: UUID =
            try { UUID.fromString(auth.name) }
            catch (e: IllegalArgumentException) {
                logger.warn("Authentication contained invalid user UUID: {}", auth)
                throw AuthenticationException("Invalid UUID", e)
            }

        val jwtAuth = auth as? JwtAuthenticationToken
            ?: run {
                logger.warn("User creation attempted without authentication type")
                throw AuthenticationException("Invalid JWT token")
            }

        val username = jwtAuth.token.getClaimAsString("preferred_username")
            ?: run {
                logger.warn("JWT token missing preferred_username claim for user id={}", uuid)
                throw AuthenticationException("Username missing from token")
            }

        val query = userRepository.getById(uuid)

        if(query.isSuccess) {
            logger.warn("User Already exists id={}", uuid)
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
                    logger.info(
                        "User created successfully id={} username={}",
                        user.id,
                        user.userName
                    )
                }.onFailure { user ->
                  logger.error(
                      "Failed to create user id={}",
                      uuid, user
                  )
                }
                .getOrThrow()
                .toDomain()
        }
    }
}