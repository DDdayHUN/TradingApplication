package data.repository.order.sql

import data.repository.order.ITradingOrderRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
class TradingOrderRepository(
    private val repository: ITradingOrderJpaRepository
) : ITradingOrderRepository {
    //===========================================================//
    //===========================================================//
    // Public Method(s)

    override suspend fun save(order: TradingOrderEntity): Result<TradingOrderEntity> {
        return runCatching {
            repository.save(order)
        }
    }

    //===========================================================//

    override suspend fun saveAll(order: Iterable<TradingOrderEntity>): Result<List<TradingOrderEntity>> {
        return runCatching {
            repository.saveAll(order)
        }
    }

    //===========================================================//

    override suspend fun getById(orderId: UUID): Result<TradingOrderEntity> {
        return runCatching {
            repository.findById(orderId).orElseThrow()
        }
    }

    //===========================================================//

    override suspend fun getAllById(orderIds: Iterable<UUID>): Result<List<TradingOrderEntity>> {
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
}