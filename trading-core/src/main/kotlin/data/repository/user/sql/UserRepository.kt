package data.repository.user.sql

import data.repository.user.IUserRepository
import domain.User
import exception.api.UserNotFoundException
import org.springframework.stereotype.Repository
import java.util.*

@Repository
class UserRepository(
    private val repository: IUserJpaRepository
) : IUserRepository {

    override suspend fun getAll(): Result<List<User>> {
        return runCatching {
            repository.findAll().map{ user ->
                user.toDomain()
            }
        }
    }

    override suspend fun getById(id: UUID): Result<User> {
        return runCatching {
            val entity = repository.findById(id)
                .orElseThrow { UserNotFoundException(id) }

            entity.toDomain()
        }
    }

    override suspend fun save(user: User): Result<User> {
        return runCatching {
            repository.save(user.toEntity()).toDomain()
        }
    }
}