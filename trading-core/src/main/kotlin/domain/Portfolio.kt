package domain

import domain.trader.Trader
import java.util.UUID

class Portfolio {
    val id: UUID
    val userId: UUID
    val traders: List<Trader>
    val availableCash: Double

    constructor(id: UUID = UUID.randomUUID(),userId: UUID, traders: List<Trader>, availableCash: Double) {
        this.id = id
        this.userId = userId // Igy lesz konnyebb a jovoben valtani
        this.traders = traders
        this.availableCash = availableCash
    }
}