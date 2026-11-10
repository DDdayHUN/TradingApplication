package domain.trader

import domain.market.security.SecurityHolding

data class SellHolding(
    val securityHolding: SecurityHolding,
    val amount: Int
) {
    init {
        require(amount > 0) { "Non-positive amount" }
        require(amount <= securityHolding.amount) { "Amount is greater than security holding amount" }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is SellHolding) return false
        return securityHolding == other.securityHolding
    }

    override fun hashCode(): Int {
        return securityHolding.hashCode()
    }
}