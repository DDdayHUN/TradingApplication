package data.persistence.mapper

import api.dto.SecurityHoldingResponse
import api.dto.SecurityIdentifierResponse
import api.dto.TraderResponse
import data.persistence.entity.PortfolioEntity
import data.persistence.entity.TraderEntity
import data.persistence.entity.security.SecurityHoldingEntity
import data.persistence.entity.security.SecurityIdentifierEntity
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
    private val algorithmStateMapType =
        object : TypeToken<Map<String, Any?>>() {}.type


    fun serializeAlgorithm(algorithm: ITradingAlgorithm): String{
        return gson.toJson(
            algorithm,
            ITradingAlgorithm::class.java,
        )
    }

    fun deserializeAlgorithm(algorithmState: String): ITradingAlgorithm{
        return gson.fromJson(
            algorithmState,
            ITradingAlgorithm::class.java,
        )
    }

    fun deserializeAlgorithmState(algorithmState: String): Map<String, Any?>{
        return gson.fromJson(
            algorithmState,
            algorithmStateMapType
        )
    }

    //===========================================================//

    fun toEntity(trader: Trader, portfolio: PortfolioEntity, algorithmType: String): TraderEntity {
        val entity = TraderEntity(
            id = trader.uuid,
            securityIdentifier = SecurityIdentifierEntity(
                isin = trader.securityIdentifier.isin,
                tickerSymbol = trader.securityIdentifier.tickerSymbol,
                currency = trader.securityIdentifier.currency,
            ),
            portfolio = portfolio,
            capital = trader.capital,
            algorithmType = algorithmType,
            algorithmState = gson.toJson(
                trader.algorithm,
                ITradingAlgorithm::class.java
            )
        )

        trader.holdings.forEach { holding ->
            entity.addHolding(
                SecurityHoldingEntity(
                    id = holding.uuid,
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
                    uuid = holding.id,
                    entryPrice = holding.entryPrice,
                    amount = holding.amount,
                )
            }.toMutableList()
        return Trader(
            uuid = entity.id,
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

    //===========================================================//

    fun toResponse(entity: TraderEntity): TraderResponse {
        val state = gson.fromJson<Map<String, Any?>>(
            entity.algorithmState,
            algorithmStateMapType
        )

        return TraderResponse(
            id = entity.id,
            securityIdentifier = SecurityIdentifierResponse(
                isin = entity.securityIdentifier.isin,
                tickerSymbol = entity.securityIdentifier.tickerSymbol,
                currency = entity.securityIdentifier.currency
            ),
            capital = entity.capital,
            holdings = entity.holdings.map {holding ->
                SecurityHoldingResponse(
                    id = holding.id,
                    entryPrice = holding.entryPrice,
                    amount = holding.amount,
                )
            },
            algorithm = state
        )
    }
}