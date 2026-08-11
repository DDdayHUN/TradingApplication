package api.dto.security

data class SecurityIdentifierRequest(
    val isin: String,
    val tickerSymbol: String,
    val currency: String
)
