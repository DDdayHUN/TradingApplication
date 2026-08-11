package api.dto

import java.util.UUID

data class SecurityHoldingResponse(
    val id: UUID,
    val entryPrice: Double,
    val amount: Int
)

data class SecurityIdentifierRequest(
    val isin: String,
    val tickerSymbol: String,
    val currency: String
)

data class SecurityIdentifierResponse(
    val isin: String,
    val tickerSymbol: String,
    val currency: String
)