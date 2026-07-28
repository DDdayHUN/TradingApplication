package api.service

import api.dto.user.CreateUserRequest
import api.dto.user.UserResponse
import api.entity.UserEntity
import api.exception.user.UserAlreadyExistsException
import api.exception.user.UserNotFoundException
import api.mapper.UserMapper
import api.repository.IUserRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class UserService {
    //===========================================================//
    //===========================================================//
    // Private Field(s)

    private val userRepository: IUserRepository
    private val userMapper: UserMapper

    //===========================================================//
    //===========================================================//
    // Public Method(es)

    fun create(request: CreateUserRequest): UserResponse {
        if(userRepository.findByKeycloakSub(request.keycloakSub) != null) {
            throw UserAlreadyExistsException(request.keycloakSub)
        }

        val user = UserEntity(
            keycloakSub = request.keycloakSub,
        )

        return userMapper.toResponse(
            userRepository.save(user)
        )
    }

    //===========================================================//

    fun findAll(): List<UserResponse>{
        return userRepository.findAll()
            .map(userMapper::toResponse)
    }

    //===========================================================//

    fun findByKeycloakSub(keycloakSub: String): UserResponse{
        val user = userRepository.findByKeycloakSub(keycloakSub)?:
        throw UserNotFoundException(keycloakSub)

        return userMapper.toResponse(user)
    }

    //===========================================================//

    fun findById(id: UUID): UserResponse{
        val user = userRepository.findByIdOrNull(id)?:
        throw UserNotFoundException(id)

        return userMapper.toResponse(user)
    }

    //===========================================================//
    //===========================================================//
    // Constructor(s)
    constructor(userRepository: IUserRepository, userMapper: UserMapper) {
        this.userRepository = userRepository
        this.userMapper = userMapper
    }
}