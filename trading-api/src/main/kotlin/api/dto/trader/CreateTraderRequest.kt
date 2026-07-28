package api.dto.trader

data class CreateTraderRequest(
    val securityIdentifier: SecurityIdentifierRequest,
    val capital: Double,
    val algorithmType: String
)
