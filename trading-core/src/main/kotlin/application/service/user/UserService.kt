package application.service.user

import data.repository.user.IUserRepository
import data.repository.user.sql.UserEntity
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
class UserService(
    private val userRepository: IUserRepository
): IUserService {
    //===========================================================//
    //===========================================================//
    // Public Method(s)

    @Transactional(readOnly = true)
    override suspend fun getAllUser(): List<UserEntity> {
        return userRepository.getAll().getOrThrow()
    }
}