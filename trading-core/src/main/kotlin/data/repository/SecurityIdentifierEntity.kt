package data.repository

import domain.market.security.SecurityIdentifier
import jakarta.persistence.Embeddable

@Embeddable
class SecurityIdentifierEntity (
    var isin: String,
    var tickerSymbol: String,
    var currency: String,
)

fun SecurityIdentifier.toEntity(): SecurityIdentifierEntity {
    return SecurityIdentifierEntity(
        isin = isin,
        tickerSymbol = tickerSymbol,
        currency = currency
    )
}

fun SecurityIdentifierEntity.toDomain(): SecurityIdentifier {
    return SecurityIdentifier(
        isin = isin,
        tickerSymbol = tickerSymbol,
        currency = currency
    )
}