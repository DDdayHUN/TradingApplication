package data.repository.user.sql

import data.repository.user.IUserRepository
import exception.UserNotFoundException
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

    override suspend fun saveAll(users: Iterable<UserEntity>): Result<List<UserEntity>> {
        return runCatching {
            repository.saveAll(users)
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

    override suspend fun getAllById(userIds: Iterable<UUID>): Result<List<UserEntity>> {
        return runCatching {
            repository.findAllById(userIds)
        }
    }

    //===========================================================//

    override suspend fun deleteById(userId: UUID): Result<Unit> {
        return runCatching {
            repository.deleteById(userId)
        }
    }

    //===========================================================//

    override suspend fun deleteAllById(userIds: Iterable<UUID>): Result<Unit> {
        return runCatching {
            repository.deleteAllById(userIds)
        }
    }

    //===========================================================//

    override suspend fun deleteAll(): Result<Unit> {
        return runCatching {
            repository.deleteAll()
        }
    }
}