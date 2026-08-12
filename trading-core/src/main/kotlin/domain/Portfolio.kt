package domain

import domain.trader.Trader
import java.util.UUID

class Portfolio {
    val id: UUID = UUID.randomUUID()
    val userId: UUID
    val traders: List<Trader>
    val availableCash: Double

    constructor(userId: UUID, traders: List<Trader>, availableCash: Double) {
        this.userId = userId // Igy lesz konnyebb a jovoben valtani
        this.traders = traders
        this.availableCash = availableCash
    }
}