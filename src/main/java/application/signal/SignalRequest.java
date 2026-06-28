package application.signal;

import domain.algorithm.Algorithm;
import domain.stock.Holding;

import java.util.List;

public record SignalRequest(
  String symbol,
  Algorithm algorithm,
  List<Holding> holdings,
  double availableCapital,
  double currentPrice
) {
}
