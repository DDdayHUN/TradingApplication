package api.dto.trader

import api.dto.security.SecurityHoldingResponse
import api.dto.security.SecurityIdentifierResponse
import java.util.UUID

data class TraderResponse(
    val id: UUID,
    val securityIdentifier: SecurityIdentifierResponse,
    val capital: Double,
    val holdings: List<SecurityHoldingResponse>,
    val algorithmType: String,
    val algorithmState: Map<String, Any?>
)
