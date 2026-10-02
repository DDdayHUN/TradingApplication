package application.service.user

import application.model.User
import data.repository.user.IUserRepository
import data.repository.user.sql.toDomain
import org.springframework.stereotype.Service
import java.util.*

@Service
class UserService(
    private val userRepository: IUserRepository
): IUserService {
    override suspend fun getById(userId: UUID): User {
        return userRepository.getById(userId).getOrThrow().toDomain()
    }
}