package domain

import domain.trader.Trader

class Portfolio {
    val user: User
    val traders: List<Trader>
    val availableCash: Double

    constructor(user: User, traders: List<Trader>, availableCash: Double) {
        this.user = user
        this.traders = traders
        this.availableCash = availableCash
    }
}