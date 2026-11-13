package domain.trader

import domain.market.security.SecurityHolding

data class SellSecurityHolding(
    val securityHolding: SecurityHolding,
    val amount: Int
) {
    init {
        require(amount > 0)
        require(amount <= securityHolding.amount)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is SellSecurityHolding) return false
        return securityHolding == other.securityHolding
    }

    override fun hashCode(): Int {
        return securityHolding.hashCode()
    }
}