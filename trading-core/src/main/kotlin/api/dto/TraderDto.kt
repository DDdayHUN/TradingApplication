package api.dto

import domain.trader.Trader
import java.util.UUID

data class ChangeTraderAlgorithmRequest(
    val algorithmType: String
)

data class CreateTraderRequest(
    val securityIdentifier: SecurityIdentifierRequest,
    val capital: Double,
    val algorithmType: String
)

data class TraderResponse(
    val id: UUID,
    val securityIdentifier: SecurityIdentifierResponse,
    val capital: Double,
    val holdings: List<SecurityHoldingResponse>
)

fun Trader.toResponse(): TraderResponse {
    return TraderResponse(
        id = id,
        securityIdentifier = SecurityIdentifierResponse(
            isin = securityIdentifier.isin,
            tickerSymbol = securityIdentifier.tickerSymbol,
            currency = securityIdentifier.currency
        ),
        capital = capital,
        holdings = holdings.map { holding ->
            SecurityHoldingResponse(
                id = holding.id,
                entryPrice = holding.entryPrice,
                amount = holding.amount
            )
        }
    )
}