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
    override suspend fun getById(traderId: UUID): Result<Trader> {
        return runCatching {
            traderRepository.findByIdWithHoldings(traderId)
                ?: throw TraderNotFoundException(traderId)
        }.map { trader ->
            trader.toDomain()
        }
    }

    override suspend fun save(trader: Trader): Result<Trader> {
        return runCatching {
           val entity = traderRepository.findByIdWithHoldings(trader.id)
               ?:throw TraderNotFoundException(trader.id)

            entity.updateFrom(trader)

            traderRepository.save(entity).toDomain()
        }
    }

    override suspend fun delete(traderId: UUID): Result<Unit> {
        return runCatching {
            val entity = traderRepository.findByIdWithHoldings(traderId)
                ?: throw TraderNotFoundException(traderId)

            if (entity.holdings.isNotEmpty()) throw TraderHoldingsNotEmptyException(traderId)
            traderRepository.deleteById(traderId)
        }
    }

}