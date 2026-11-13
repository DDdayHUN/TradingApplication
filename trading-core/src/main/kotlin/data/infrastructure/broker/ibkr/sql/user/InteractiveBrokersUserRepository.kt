package data.infrastructure.broker.ibkr.sql.user

import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
class InteractiveBrokersUserRepository(
    private val repository: IInteractiveBrokersUserJpaRepository
) : IInteractiveBrokersUserRepository {
    //===========================================================//
    //===========================================================//
    // Public Method(s)

    override suspend fun save(user: InteractiveBrokersUserEntity): Result<InteractiveBrokersUserEntity> {
        return runCatching {
            repository.save(user)
        }
    }

    //===========================================================//

    override suspend fun saveAll(users: Iterable<InteractiveBrokersUserEntity>): Result<List<InteractiveBrokersUserEntity>> {
        return runCatching {
            repository.saveAll(users)
        }
    }

    //===========================================================//

    override suspend fun getById(userId: UUID): Result<InteractiveBrokersUserEntity> {
        return runCatching {
            repository.findById(userId).orElseThrow()
        }
    }

    //===========================================================//

    override suspend fun getAllById(userIds: Iterable<UUID>): Result<List<InteractiveBrokersUserEntity>> {
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

    //===========================================================//

    override suspend fun fetchNextId(): Result<Int> {
        return runCatching {
            repository.findLargestClientId() + 1
        }
    }
}