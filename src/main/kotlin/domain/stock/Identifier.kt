package domain.stock

//===========================================================//

/**
 * Represents a financial instrument identifier consisting of an ISIN,
 * an exchange code, and a ticker symbol.
 * 
 * The ISIN provides a globally unique identifier for the security,
 * while the exchange and symbol define its specific listing context.
 * 
 * @param isin - the 12-character International Securities Identification Number (ISIN).
 * @param mic - the market identifier code where the instrument is traded.
 * @param tickerSymbol - the ticker symbol of the instrument on the given exchange.
 * @param currency - the currency in which the given instrument is traded at.
 */
//===========================================================//

data class Identifier(
    val isin: String,
    val mic: String,
    val tickerSymbol: String,
    val currency: String
) {
    init {
        require(isin.matches(Regex("[A-Za-z]{2}[A-Za-z0-9]{9}[0-9]"))) { "ISIN" }
        require(mic.matches(Regex("[A-Za-z0-9]{4}"))) { "MIC" }
        require(!tickerSymbol.isBlank()) { "Ticker Symbol" }
        require(currency.matches(Regex("[A-Za-z]{3}"))) { "Currency" }
    }
}
