package domain.signal

import java.time.Instant

//===========================================================//
/**
 * Represents formatted trading signal that can be displayed
 */
//===========================================================//

data class TradingSignal(
    val symbol: String,
    val action: Action,
    val strength: Strength,
    val currentPrice: Double,
    val amount: Long?,
    val currentStockCount: Long,
    val reason: String,
    val createdAt: Instant
) {
    //===========================================================//
    //===========================================================//
    // Public Method(es)

    fun formatToReadableText(): String {
        val amountText = if(amount == null) "" else " | Amount:  $amount"

        return (symbol + ": "
                + action
                + " | "
                + strength
                + " | Price: "
                + String.format("%.2f", currentPrice)
                + amountText
                + " | Current Stock Count: "
                + currentStockCount
                + " | Reason: "
                + reason
                + " | At: "
                + createdAt)
    }

    //===========================================================//
    //===========================================================//
    // Enum(s)

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
