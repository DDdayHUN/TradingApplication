package domain.signal;

import java.time.Instant;


//===========================================================//
/**
 * Represents formatted trading signal that can be displayed
 */
//===========================================================//
public record TradingSignal(
  String symbol,
  SignalAction action,
  SignalStrength strength,
  double currentPrice,
  Long amount,
  Long currentStockCount,
  String reason,
  Instant createdAt
) {
   //===========================================================//
   /**
    * Represents the calculated Signal strength of generated signal.
    */
   //===========================================================//
   public enum SignalStrength {
      HIGH,
      MEDIUM,
      LOW
   }

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
}
