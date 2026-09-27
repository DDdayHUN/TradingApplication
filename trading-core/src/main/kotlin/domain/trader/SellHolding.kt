package domain.trader

import java.util.*

data class SellHolding(
    val id: UUID,
    val amount: Int
) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is SellHolding) return false
        return id == other.id
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }
}