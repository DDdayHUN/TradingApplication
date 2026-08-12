package data.repository.trader.sql

import api.dto.SecurityHoldingResponse
import api.dto.SecurityIdentifierResponse
import api.dto.TraderResponse
import data.repository.portfolio.PortfolioEntity
import data.repository.SecurityHoldingEntity
import data.repository.SecurityIdentifierEntity
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import domain.algorithm.ITradingAlgorithm
import domain.market.security.SecurityHolding
import domain.market.security.SecurityIdentifier
import domain.trader.Trader
import org.springframework.stereotype.Component

@Component
class TraderMapper (
    private val gson: Gson
) {
    fun toEntity(trader: Trader, portfolio: PortfolioEntity): TraderEntity {
        val entity = TraderEntity(
            id = trader.id,
            securityIdentifier = SecurityIdentifierEntity(
                isin = trader.securityIdentifier.isin,
                tickerSymbol = trader.securityIdentifier.tickerSymbol,
                currency = trader.securityIdentifier.currency,
            ),
            portfolio = portfolio,
            capital = trader.capital,
            algorithmType = ITradingAlgorithm.typeTagOf(trader.algorithm),
            algorithmState = gson.toJson(
                trader.algorithm,
                ITradingAlgorithm::class.java
            )
        )

        trader.holdings.forEach { holding ->
            entity.addHolding(
                SecurityHoldingEntity(
                    id = holding.id,
                    entryPrice = holding.entryPrice,
                    amount = holding.amount,
                    trader = entity
                )
            )
        }

        return entity
    }

    //===========================================================//

    fun toDomain(entity: TraderEntity): Trader {
        val algorithm = gson.fromJson(
            entity.algorithmState,
            ITradingAlgorithm::class.java
        )

        val domainHoldings = entity.holdings
            .map { holding ->
                SecurityHolding(
                    id = holding.id,
                    entryPrice = holding.entryPrice,
                    amount = holding.amount,
                )
            }.toMutableList()
        return Trader(
            id = entity.id,
            securityIdentifier = SecurityIdentifier(
                isin = entity.securityIdentifier.isin,
                tickerSymbol = entity.securityIdentifier.tickerSymbol,
                currency = entity.securityIdentifier.currency
            ),
            holdings = domainHoldings,
            allocatedCapital = entity.capital,
            algorithm = algorithm,
        )
    }
}