package data.repository.trader

import domain.assets.security.SecurityHolding
import domain.assets.security.SecurityIdentifier

data class TraderDto(
    val securityIdentifier: SecurityIdentifier,
    val holdings: List<SecurityHolding>,
    val allocatedCapital: Double,
    val algorithmType: String
    )
