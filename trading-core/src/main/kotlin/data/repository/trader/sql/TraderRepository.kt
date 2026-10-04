package data.repository.trader.sql

import data.repository.trader.ITraderRepository
import domain.trader.Trader
import exception.api.TraderHoldingsNotEmptyException
import exception.api.TraderNotFoundException
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
class TraderRepository(
    private val traderRepository: ITraderJpaRepository
) : ITraderRepository {
    //===========================================================//
    //===========================================================//
    // Public Method(s)

    override suspend fun getById(traderId: UUID): Result<TraderEntity> {
        return runCatching {
            traderRepository.findByIdWithHoldings(traderId)
                ?: throw TraderNotFoundException(traderId)
        }
    }

    //===========================================================//

    override suspend fun save(trader: TraderEntity): Result<TraderEntity> {
        return runCatching {
            traderRepository.save(trader)
        }
    }

    //===========================================================//

    override suspend fun deleteById(traderId: UUID): Result<Unit> {
        return runCatching {
            val entity = traderRepository.findByIdWithHoldings(traderId)
                ?: throw TraderNotFoundException(traderId)

            if (entity.holdings.isNotEmpty()) throw TraderHoldingsNotEmptyException(traderId)
            traderRepository.deleteById(entity.id)
        }
    }
}