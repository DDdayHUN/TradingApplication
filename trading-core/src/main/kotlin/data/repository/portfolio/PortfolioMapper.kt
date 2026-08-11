package data.repository.portfolio

import api.dto.PortfolioResponse
import data.repository.trader.sql.TraderMapper
import data.repository.user.UserMapper
import org.springframework.stereotype.Component

@Component
class PortfolioMapper {

    private val traderMapper: TraderMapper
    private val userMapper: UserMapper

    fun toResponse(portfolio: PortfolioEntity): PortfolioResponse {
        return PortfolioResponse(
            user = userMapper.toResponse(portfolio.user),
            availableCash = portfolio.availableCash,
            traders = portfolio.traders.map(
                traderMapper::toResponse
            )
        )
    }

    constructor(traderMapper: TraderMapper, userMapper: UserMapper) {
        this.traderMapper = traderMapper
        this.userMapper = userMapper
    }
}