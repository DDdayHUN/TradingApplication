package domain.signal;

import java.time.Instant;

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
