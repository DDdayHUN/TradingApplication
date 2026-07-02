package domain.signal

import java.time.Instant

//===========================================================//
/**
 * Represents formatted trading signal that can be displayed
 */
//===========================================================//

data class TradingSignal(
    val action: Action,
    val strength: Strength,
    val currentPrice: Double,
    val amount: Long?,
    val currentStockCount: Long,
    val reason: String,
    val createdAt: Instant
) {
    /**
     * Represents the calculated Signal strength of generated signal.
     */
    enum class Strength {
        HIGH,
        MEDIUM,
        LOW
    }

    //===========================================================//
    /**
     * Represents the action suggested by the signal engine.
     */
    enum class Action {
        BUY,
        SELL,
        HOLD
    }
}
