package application.signal;

import domain.algorithm.Algorithm;
import domain.stock.Holding;

import java.util.List;

//===========================================================//
/**
 * Contains all input data required to generate a trading signal.
 */
//===========================================================//
public record SignalRequest(
  String symbol,
  Algorithm algorithm,
  List<Holding> holdings,
  double availableCapital,
  double currentPrice
) {
}
