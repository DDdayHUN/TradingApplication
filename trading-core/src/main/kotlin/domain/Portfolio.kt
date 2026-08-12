package domain

import domain.trader.Trader
import java.util.UUID

class Portfolio {
    val id: UUID
    val traders: List<Trader>
    val availableCash: Double

    constructor(user: User, traders: List<Trader>, availableCash: Double) {
        this.id = user.id   // Igy van most az 1 : 1 kapcsolat megcsinalva, es ez jovoben majd tud nagyon egyszeruen valtozni.
        this.traders = traders
        this.availableCash = availableCash
    }
}