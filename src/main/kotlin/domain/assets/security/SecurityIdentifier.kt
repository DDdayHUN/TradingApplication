package domain.assets.security

//===========================================================//
/**
 * Represents a financial instrument identifier consisting of an ISIN,
 * an exchange code, and a ticker symbol.
 * 
 * The ISIN provides a globally unique identifier for the security,
 * while the exchange and symbol define its specific listing context.
 *
 * @param isin the 12-character International Securities Identification Number (ISIN).
 * @param tickerSymbol the ticker-symbol of the given security. (i.e: AAPL).
 * @param currency the currency in which the security is traded in. (i.e: USD).
 */
//===========================================================//

data class SecurityIdentifier(
    val isin: String,
    val tickerSymbol: String,
    val currency: String
) {
    init {
        require(isin.matches(Regex("[A-Z]{2}[A-Z0-9]{9}[0-9]"))) { "ISIN" }
        require(!tickerSymbol.isBlank() && tickerSymbol.length in 1..20) { "TickerSymbol" }
        require(currency.matches(Regex("[A-Za-z]{3}"))) { "Currency" }
    }
}