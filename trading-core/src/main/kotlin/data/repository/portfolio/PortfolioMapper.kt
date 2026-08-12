package data.repository.portfolio

import data.repository.trader.sql.TraderMapper
import data.repository.user.UserEntity
import data.repository.user.UserMapper
import domain.Portfolio
import org.springframework.stereotype.Component

@Component
class PortfolioMapper {

    private val traderMapper: TraderMapper
    private val userMapper: UserMapper

    fun toDomain(portfolioEntity: PortfolioEntity): Portfolio {
        return Portfolio(
            userId = requireNotNull(portfolioEntity.user.id),
            traders = portfolioEntity.traders.map(traderMapper::toDomain),
            availableCash = portfolioEntity.availableCash,
        )
    }

    fun toEntity(portfolio: Portfolio, userEntity: UserEntity) : PortfolioEntity {
         val entity = PortfolioEntity(
             id = portfolio.id,
             user = userEntity,
             availableCash = portfolio.availableCash
         )

        portfolio.traders.forEach { trader ->
            entity.addTrader(traderMapper.toEntity(
                trader = trader,
                portfolio = entity
            ))
        }

        return entity
    }

    constructor(traderMapper: TraderMapper, userMapper: UserMapper) {
        this.traderMapper = traderMapper
        this.userMapper = userMapper
    }
}