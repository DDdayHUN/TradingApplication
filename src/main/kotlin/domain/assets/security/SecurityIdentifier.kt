package domain.assets.security

import java.util.UUID

//===========================================================//
/**
 * Represents a financial instrument identifier consisting of an ISIN,
 * an exchange code, and a ticker symbol.
 * 
 * The ISIN provides a globally unique identifier for the security,
 * while the exchange and symbol define its specific listing context.
 *
 * @param isin the 12-character International Securities Identification Number (ISIN).
 * @param name the informal name of the security (i.e: Apple).
 * @param currency the currency in which the security is traded in. (i.e: USD)
 */
//===========================================================//

data class SecurityIdentifier(
    val uuid: UUID,
    val isin: String,
    val currency: String,
    val name: String
) {
    constructor(isin: String, currency: String, name: String) : this(UUID.randomUUID(), isin, currency, name)

    init {
        require(isin.matches(Regex("[A-Z]{2}[A-Z0-9]{9}[0-9]"))) { "ISIN" }
        require(currency.matches(Regex("[A-Za-z]{3}"))) { "Currency" }
        require(!name.isBlank() && name.length in 1..20) { "Name" }
    }
}