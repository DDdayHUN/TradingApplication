package api.dto.trader

data class SecurityIdentifierResponse(
    val isin: String,
    val tickerSymbol: String,
    val currency: String
)
