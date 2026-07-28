package api.dto.trader

data class SecurityIdentifierRequest(
    val isin: String,
    val tickerSymbol: String,
    val currency: String
)
