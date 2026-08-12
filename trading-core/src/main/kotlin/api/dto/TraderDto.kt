package api.dto

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