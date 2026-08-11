package data.persistence.mapper

import api.dto.portfolio.PortfolioResponse
import data.persistence.entity.PortfolioEntity
import org.springframework.stereotype.Component

@Component
class PortfolioMapper {

    private val traderMapper: TraderMapper
    private val userMapper: UserMapper

    fun toResponse(portfolio: PortfolioEntity): PortfolioResponse{
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