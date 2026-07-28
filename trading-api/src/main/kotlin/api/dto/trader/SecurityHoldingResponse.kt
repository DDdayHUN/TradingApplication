package api.dto.trader

import java.util.UUID

data class SecurityHoldingResponse(
    val id: UUID,
    val entryPrice: Double,
    val amount: Int
)
