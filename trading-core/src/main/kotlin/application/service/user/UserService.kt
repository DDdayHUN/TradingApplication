package application.service.user

import data.repository.user.IUserRepository
import data.repository.user.sql.toDomain
import data.repository.user.sql.update
import org.springframework.stereotype.Service
import java.util.*

@Service
class UserService(
    private val userRepository: IUserRepository
): IUserService {
    //===========================================================//
    //===========================================================//
    // Public Method(s)

    override suspend fun update(user: User): User {
        val userEntity = userRepository.getById(user.id).getOrThrow()

        userEntity.update(user)
        return userRepository.save(userEntity).getOrThrow().toDomain()
    }

    //===========================================================//

    override suspend fun getById(userId: UUID): User {
        return userRepository.getById(userId).getOrThrow().toDomain()
    }
}