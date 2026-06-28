package application.signal;

import domain.signal.TradingSignal;

//===========================================================//
/**
 * Formats trading signals into readable text output.
 */
//===========================================================//
public final class SignalFormatter {

   public String format(final TradingSignal signal) {
      final String amountText = signal.amount() == null
        ? "" : " | Amount:  " + signal.amount();

      return signal.symbol()
               + ": "
               + signal.action()
               + " | "
               + signal.strength()
               + " | Price: "
               + String.format("%.2f", signal.currentPrice())
               + amountText
               + " | Current Stock Count: "
               + signal.currentStockCount()
               + " | Reason: "
               + signal.reason()
               + " | At: "
               + signal.createdAt();
   }
}
