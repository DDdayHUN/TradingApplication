package api.dto.trader

import api.dto.security.SecurityIdentifierRequest

data class CreateTraderRequest(
    val securityIdentifier: SecurityIdentifierRequest,
    val capital: Double,
    val algorithmType: String
)
