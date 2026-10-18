package api.dto

import application.service.broker.InteractiveBrokersOrder
import application.service.broker.InteractiveBrokersOrder.Action
import application.service.broker.InteractiveBrokersOrder.Status
import java.util.*
import java.time.Instant

data class OrderResponse(
    val id: UUID,
    val securityIdentifier: SecurityIdentifierResponse,
    val action: Action,
    val status: Status,
    val signalPrice: Double,
    val filledPrice: Double?,
    val timestamp: Instant
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
        filledPrice = averageFillPrice,
        timestamp = createdAt
    )
}