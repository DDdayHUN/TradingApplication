package api.dto

import domain.trader.TradingOrder
import java.util.*
import java.time.Instant

//===========================================================//
//===========================================================//

data class OrderResponse(
    val id: UUID,
    val securityIdentifier: SecurityIdentifierResponse,
    val signalPrice: Double,
    val timestamp: Instant
)

//===========================================================//

fun TradingOrder.toResponse(): OrderResponse {
    return OrderResponse(
        id = orderId,
        securityIdentifier = SecurityIdentifierResponse(
            isin = securityIdentifier.isin,
            tickerSymbol = securityIdentifier.tickerSymbol,
            currency = securityIdentifier.currency,
        ),
        signalPrice = atPrice,
        timestamp = createdAt
    )
}