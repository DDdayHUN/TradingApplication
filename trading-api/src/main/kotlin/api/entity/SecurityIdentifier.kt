package api.entity

import jakarta.persistence.Column
import jakarta.persistence.Embeddable

@Embeddable
class SecurityIdentifier (

    @Column(name = "security_isin", length = 12, nullable = false)
    var isin: String,

    @Column(name = "security_ticker", length  = 20, nullable = false)
    var tickerSymbol: String,

    @Column(name = "security_currency", length = 3, nullable = false)
    var currency: String,
)