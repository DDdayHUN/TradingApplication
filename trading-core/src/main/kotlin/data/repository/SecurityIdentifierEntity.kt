package data.repository

import jakarta.persistence.Embeddable

@Embeddable
class SecurityIdentifierEntity (
    var isin: String,
    var tickerSymbol: String,
    var currency: String,
)