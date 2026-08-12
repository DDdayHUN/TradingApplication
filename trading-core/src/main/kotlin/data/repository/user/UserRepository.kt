package data.repository.user

import domain.User
import domain.interfaces.IUserRepository
import exception.api.UserNotFoundException
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
class UserRepository : IUserRepository {
    private val repository: IUserJpaRepository
    private val mapper : UserMapper

    override suspend fun save(user: User): Result<Unit> {
        return runCatching {
            repository.save(mapper.toEntity(user))
        }
    }

    override suspend fun getById(id: UUID): Result<User> {
        return runCatching {
            val entity = repository.findById(id)
                .orElseThrow { UserNotFoundException(id) }

            mapper.toDomain(entity)
        }
    }

    override suspend fun getAll(): Result<List<User>> {
        return runCatching {
            repository.findAll().map(mapper::toDomain)
        }
    }

    constructor(userJpaRepository: IUserJpaRepository, mapper: UserMapper) {
        this.repository = userJpaRepository
        this.mapper = mapper
    }
}