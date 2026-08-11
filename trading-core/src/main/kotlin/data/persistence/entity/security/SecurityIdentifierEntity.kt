package data.persistence.entity.security

import jakarta.persistence.Embeddable

@Embeddable
class SecurityIdentifierEntity (
    var isin: String,
    var tickerSymbol: String,
    var currency: String,
)