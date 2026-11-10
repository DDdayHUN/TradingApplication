package domain.trader

import domain.market.security.SecurityIdentifier
import java.time.Instant
import java.util.*

data class TradingOrder(
    val id: UUID = UUID.randomUUID(),
    val traderId: UUID,
    val securityIdentifier: SecurityIdentifier,
    val signal: Signal,
    val atPrice: Double,
    val createdAt: Instant = Instant.now(),
) {
    sealed interface Signal {
        data class Buy(val amount: Int) : Signal {
            init {
                require(amount > 0)
            }
        }
        data class Sell(val sellHoldings: Set<SellSecurityHolding>) : Signal {
            init {
                require(sellHoldings.isNotEmpty())
            }
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is TradingOrder) return false
        return id == other.id
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }
}