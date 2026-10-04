package data.repository.user.sql

import data.repository.user.IUserRepository
import exception.api.UserNotFoundException
import org.springframework.stereotype.Repository
import java.util.*

@Repository
class UserRepository(
    private val repository: IUserJpaRepository
) : IUserRepository {
    //===========================================================//
    //===========================================================//
    // Public Method(s)

    override suspend fun save(user: UserEntity): Result<UserEntity> {
        return runCatching {
            repository.save(user)
        }
    }

    //===========================================================//

    override suspend fun getById(userId: UUID): Result<UserEntity> {
        return runCatching {
            val entity = repository.findById(userId)
                .orElseThrow { UserNotFoundException(userId) }

            entity
        }
    }

    //===========================================================//

    override suspend fun getAll(): Result<List<UserEntity>> {
        return runCatching {
            repository.findAll()
        }
    }

    //===========================================================//

    override suspend fun deleteById(userId: UUID): Result<Unit> {
        return runCatching {
            repository.deleteById(userId)
        }
    }

    //===========================================================//

    override suspend fun deleteAll(): Result<Unit> {
        return runCatching {
            repository.deleteAll()
        }
    }
}