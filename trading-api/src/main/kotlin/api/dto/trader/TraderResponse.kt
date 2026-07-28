package api.dto.trader

import java.util.UUID

data class TraderResponse(
    val id: UUID,
    val securityIdentifier: SecurityIdentifierResponse,
    val capital: Double,
    val holdings: List<SecurityHoldingResponse>,
    val algorithmType: String,
    val algorithmState: Map<String, Any?>
)
