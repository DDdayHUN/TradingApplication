package data.infrastructure.broker.ibkr.sql.order

import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
class InteractiveBrokersOrderRepository(
    private val repository: IInteractiveBrokersOrderJpaRepository,
) : IInteractiveBrokersOrderRepository {
    //===========================================================//
    //===========================================================//
    // Public Method(s)

    override suspend fun save(order: InteractiveBrokersOrderEntity): Result<InteractiveBrokersOrderEntity> {
        return runCatching {
            repository.save(order)
        }
    }

    //===========================================================//

    override suspend fun saveAll(order: Iterable<InteractiveBrokersOrderEntity>): Result<List<InteractiveBrokersOrderEntity>> {
        return runCatching {
            repository.saveAll(order)
        }
    }

    //===========================================================//

    override suspend fun getById(orderId: UUID): Result<InteractiveBrokersOrderEntity> {
        return runCatching {
            repository.findById(orderId).orElseThrow()
        }
    }

    //===========================================================//

    override suspend fun getAllById(orderIds: Iterable<UUID>): Result<List<InteractiveBrokersOrderEntity>> {
        return runCatching {
            repository.findAllById(orderIds)
        }
    }

    //===========================================================//

    override suspend fun deleteById(orderId: UUID): Result<Unit> {
        return runCatching {
            repository.deleteById(orderId)
        }
    }

    //===========================================================//

    override suspend fun deleteAllById(orderIds: Iterable<UUID>): Result<Unit> {
        return runCatching {
            repository.deleteAllById(orderIds)
        }
    }

    //===========================================================//

    override suspend fun deleteAll(): Result<Unit> {
        return runCatching {
            repository.deleteAll()
        }
    }

    //===========================================================//

    override suspend fun getAllByUserId(userId: UUID): Result<List<InteractiveBrokersOrderEntity>> {
        return runCatching {
            repository.findAllForUser(userId)
        }
    }
}