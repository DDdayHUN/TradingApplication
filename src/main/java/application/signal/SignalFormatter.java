package application.signal;

import domain.signal.TradingSignal;

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
               + " | Reason: "
               + signal.reason();
   }
}
