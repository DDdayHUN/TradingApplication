package application.service

import api.dto.UserResponse
import api.dto.toResponse
import domain.User
import domain.interfaces.IUserRepository
import exception.api.UserAlreadyExistsException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class UserService {

    //===========================================================//
    //===========================================================//
    // Private Field(s)

    private val userRepository: IUserRepository

    //===========================================================//
    //===========================================================//
    // Public Method(es)

    @Transactional
    suspend fun create(id: UUID): UserResponse {
       if(userRepository.getById(id).getOrNull() != null){
           throw UserAlreadyExistsException(id)
       }

        val user = User(id = id)

        userRepository.save(user)
            .getOrThrow()

        return user.toResponse()
    }

    //===========================================================//

    @Transactional(readOnly = true)
    suspend fun findAll(): List<UserResponse>{
        return userRepository.getAll()
            .getOrThrow()
            .map { user ->
                user.toResponse()
            }
    }

    //===========================================================//

    @Transactional(readOnly = true)
    suspend fun getById(id: UUID): UserResponse{
        return  userRepository.getById(id)
            .getOrThrow()
            .toResponse()
    }

    //===========================================================//
    //===========================================================//
    // Constructor(s)
    constructor(userRepository: IUserRepository) {
        this.userRepository = userRepository
    }
}