package api.dto.security

data class SecurityIdentifierResponse(
    val isin: String,
    val tickerSymbol: String,
    val currency: String
)
