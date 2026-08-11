package application.service

import api.dto.CreateUserRequest
import api.dto.UserResponse
import api.exception.user.UserAlreadyExistsException
import api.exception.user.UserNotFoundException
import data.repository.portfolio.PortfolioEntity
import data.repository.user.UserEntity
import data.repository.user.UserMapper
import data.repository.user.IUserRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
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

    @Transactional
    fun create(keycloakSub: String,request: CreateUserRequest): UserResponse {
        if(userRepository.findByKeycloakSub(keycloakSub) != null) {
            throw UserAlreadyExistsException(keycloakSub)
        }

        val user = UserEntity(
            keycloakSub = keycloakSub,
        )

        val portfolio = PortfolioEntity(
            user = user,
            availableCash = request.availableCash
        )

        user.attachPortfolio(portfolio)

        return userMapper.toResponse(
            userRepository.save(user)
        )
    }

    //===========================================================//

    @Transactional(readOnly = true)
    fun findAll(): List<UserResponse>{
        return userRepository.findAll()
            .map(userMapper::toResponse)
    }

    //===========================================================//

    @Transactional(readOnly = true)
    fun findByKeycloakSub(keycloakSub: String): UserResponse{
        val user = userRepository.findByKeycloakSub(keycloakSub)?:
        throw UserNotFoundException(keycloakSub)

        return userMapper.toResponse(user)
    }

    //===========================================================//

    @Transactional(readOnly = true)
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