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
  String reason,
  Instant createdAt
) {
}
