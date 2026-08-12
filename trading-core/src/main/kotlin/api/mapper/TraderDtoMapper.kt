package api.mapper

import api.dto.SecurityHoldingResponse
import api.dto.SecurityIdentifierResponse
import api.dto.TraderResponse
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import domain.algorithm.ITradingAlgorithm
import domain.trader.Trader
import org.springframework.stereotype.Component

@Component
class TraderDtoMapper {

    private val gson : Gson

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

    fun toResponse(trader: Trader): TraderResponse {
        return TraderResponse(
            id = trader.id,
            securityIdentifier = SecurityIdentifierResponse(
                isin = trader.securityIdentifier.isin,
                tickerSymbol = trader.securityIdentifier.tickerSymbol,
                currency = trader.securityIdentifier.currency,
            ),
            capital = trader.capital,
            holdings = trader.holdings.map { holding ->
                SecurityHoldingResponse(
                    id = holding.id,
                    entryPrice = holding.entryPrice,
                    amount = holding.amount
                )
            }
        )
    }

    constructor(gson : Gson) {
        this.gson = gson
    }
}