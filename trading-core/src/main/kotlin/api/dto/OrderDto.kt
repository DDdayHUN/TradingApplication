package api.dto

import application.model.InteractiveBrokersOrder
import application.model.InteractiveBrokersOrder.*
import java.util.UUID

data class OrderResponse(
    val id: UUID,
    val securityIdentifier: SecurityIdentifierResponse,
    val action: Action,
    val status: Status,
    val signalPrice: Double,
    val filledPrice: Double?,
)

fun InteractiveBrokersOrder.toResponse(): OrderResponse {
    return OrderResponse(
        id = id,
        securityIdentifier = SecurityIdentifierResponse(
            isin = securityIdentifier.isin,
            tickerSymbol = securityIdentifier.tickerSymbol,
            currency = securityIdentifier.currency,
        ),
        action = action,
        status = status,
        signalPrice = signalPrice,
        filledPrice = averageFillPrice
    )
}