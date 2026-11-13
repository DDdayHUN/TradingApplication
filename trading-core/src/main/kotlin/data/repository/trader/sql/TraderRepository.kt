package data.repository.trader.sql

import data.repository.trader.ITraderRepository
import exception.TraderHoldingsNotEmptyException
import exception.TraderNotFoundException
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
class TraderRepository(
    private val repository: ITraderJpaRepository
) : ITraderRepository {
    //===========================================================//
    //===========================================================//
    // Public Method(s)

    override suspend fun save(trader: TraderEntity): Result<TraderEntity> {
        return runCatching {
            repository.save(trader)
        }
    }

    //===========================================================//

    override suspend fun saveAll(traders: Iterable<TraderEntity>): Result<List<TraderEntity>> {
        return runCatching {
            repository.saveAll(traders)
        }
    }

    //===========================================================//

    override suspend fun getById(traderId: UUID): Result<TraderEntity> {
        return runCatching {
            repository.findByIdWithHoldings(traderId)
                ?: throw TraderNotFoundException(traderId)
        }
    }

    //===========================================================//

    override suspend fun getAllById(traderIds: Iterable<UUID>): Result<List<TraderEntity>> {
        return runCatching {
            val ids = traderIds.toList()
            val entities = repository.findAllByIdWithHoldings(ids)
            if (entities.size != ids.size) throw TraderNotFoundException(ids)

            entities
        }
    }

    //===========================================================//

    override suspend fun deleteById(traderId: UUID): Result<Unit> {
        return runCatching {
            val entity = getById(traderId).getOrThrow()
            if (entity.holdings.isNotEmpty()) throw TraderHoldingsNotEmptyException(traderId)

            repository.deleteById(entity.id)
        }
    }

    //===========================================================//

    override suspend fun deleteAllById(traderIds: Iterable<UUID>): Result<Unit> {
        return runCatching {
            val entities = getAllById(traderIds).getOrThrow()

            val traderIdsWithHoldings = entities
                .filter { it.holdings.isNotEmpty() }
                .map { it.id }

            if(traderIdsWithHoldings.isNotEmpty()) throw TraderHoldingsNotEmptyException(traderIdsWithHoldings)

            repository.deleteAllById(traderIds)
        }
    }

    //===========================================================//

    override suspend fun deleteAll(): Result<Unit> {
        return runCatching {
            repository.deleteAll()
        }
    }
}