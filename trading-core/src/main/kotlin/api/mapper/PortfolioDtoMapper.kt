package api.mapper

import api.dto.PortfolioResponse
import domain.Portfolio
import org.springframework.stereotype.Component

@Component
class PortfolioDtoMapper {
    private val traderDtoMapper : TraderDtoMapper

    fun toResponse(portfolio: Portfolio) : PortfolioResponse {
        return PortfolioResponse(
            id = portfolio.id,
            userId = portfolio.userId,
            availableCash = portfolio.availableCash,
            traders = portfolio.traders.map(traderDtoMapper::toResponse)
        )
    }

    constructor(traderDtoMapper: TraderDtoMapper) {
        this.traderDtoMapper = traderDtoMapper
    }
}