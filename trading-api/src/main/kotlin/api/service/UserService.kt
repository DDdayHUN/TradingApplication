package api.service

import api.dto.user.CreateUserRequest
import api.dto.user.UserResponse
import api.entity.UserEntity
import api.exception.UserAlreadyExistsException
import api.repository.IUserRepository
import org.springframework.stereotype.Service

@Service
class UserService {
    private val userRepository: IUserRepository

    fun create(request: CreateUserRequest): UserResponse {
        if(userRepository.findByKeycloakSub(request.keycloakSub) != null) {
            throw UserAlreadyExistsException(request.keycloakSub)
        }

        val user = UserEntity(
            keycloakSub = request.keycloakSub,
        )

        return userRepository.save(user).toResponse()
    }
    fun findAll(): List<UserResponse>{
        return userRepository.findAll()
            .map { user -> user.toResponse() }
    }

    private fun UserEntity.toResponse(): UserResponse =
        UserResponse(
            id = requireNotNull(id),
            keycloakSub = keycloakSub,
        )

    constructor(userRepository: IUserRepository) {
        this.userRepository = userRepository
    }
}