package domain.signal;

import java.time.Instant;


//===========================================================//
/**
 * Represents formatted trading signal that can be displayed
 */
//===========================================================//

public record TradingSignal(
  String symbol,
  Action action,
  Strength strength,
  double currentPrice,
  Long amount,
  Long currentStockCount,
  String reason,
  Instant createdAt
) {
   //===========================================================//
   //===========================================================//
   // Public Interface(s)

   public String formatToReadableText() {
      final String amountText = amount() == null
                                  ? "" : " | Amount:  " + amount();

      return symbol()
               + ": "
               + action()
               + " | "
               + strength()
               + " | Price: "
               + String.format("%.2f", currentPrice())
               + amountText
               + " | Current Stock Count: "
               + currentStockCount()
               + " | Reason: "
               + reason()
               + " | At: "
               + createdAt();
   }

   //===========================================================//
   //===========================================================//
   // Enum(s)

   /**
    * Represents the calculated Signal strength of generated signal.
    */
   public enum Strength {
      HIGH,
      MEDIUM,
      LOW
   }

   //===========================================================//
   /**
    * Represents the action suggested by the signal engine.
    */

   public enum Action {
      BUY,
      SELL,
      HOLD
   }
}
