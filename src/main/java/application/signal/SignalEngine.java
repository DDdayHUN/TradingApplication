package application.signal;

import domain.algorithm.Algorithm;
import domain.signal.SignalAction;
import domain.signal.SignalStrength;
import domain.signal.TradingSignal;

import java.time.Instant;

//===========================================================//
/**
 * Converts algorithm decisions into final trading signals {@link TradingSignal}.
 */
//===========================================================//
public final class SignalEngine {

   static public TradingSignal createSignal(
     final String symbol,
     final Algorithm.Output output,
     final double availableCapital,
     final double currentPrice
   ) {
      if (output.buy() != null) {
         return new TradingSignal(
           symbol,
           SignalAction.BUY,
           calculateBuyStrength(
             output.buy().amount(),
             availableCapital,
             currentPrice
           ),
           currentPrice,
           output.buy().amount(),
           "Algorithm generated a buy signal.",
           Instant.now()
         );
      }

      if (output.sell() != null) {
         final long amountToSell = output.sell()
                                     .batches()
                                     .stream()
                                     .mapToLong(batch -> batch.second)
                                     .sum();

         return new TradingSignal(
           symbol,
           SignalAction.SELL,
           SignalStrength.MEDIUM,
           currentPrice,
           amountToSell,
           "Algorithm generated a sell signal.",
           Instant.now()
         );
      }

      return new TradingSignal(
        symbol,
        SignalAction.HOLD,
        SignalStrength.LOW,
        currentPrice,
        null,
        "No buy or sell signal was generated.",
        Instant.now()
      );
   }

   static private SignalStrength calculateBuyStrength(
     final long amount,
     final double availableCapital,
     final double currentPrice
   ) {
      if (availableCapital <= 0.0d) {
         return SignalStrength.LOW;
      }

      final double usedCapital = amount * currentPrice;
      final double usedCapitalRatio = usedCapital / availableCapital;

      if (usedCapitalRatio >= 0.20d) {
         return SignalStrength.HIGH;
      }

      if (usedCapitalRatio >= 0.10d) {
         return SignalStrength.MEDIUM;
      }

      return SignalStrength.LOW;
   }
}