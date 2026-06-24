package domain.algorithm;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import domain.stock.Bought;
import domain.stock.History;
import utils.Pair;

final class TACPP46 extends Algorithm {
    /*===========================================================*/
    /*===========================================================*/
    // Private Field(s)

    private ArrayDeque<Double>  lastInputArr        = new ArrayDeque<>();
    private ArrayDeque<Double>  emaHistory          = new ArrayDeque<>();

    private Map<Bought, Double> trailingHigh        = new HashMap<>();
    private List<Bought>        markedForSelling    = new ArrayList<>();

    /*===========================================================*/
    /*===========================================================*/
    // Public Interface(s)

    @Override
    public AlgorithmOutput Run(final List<Bought> holdings, final double allocatedToke, final double currentPrice) {
        AlgorithmOutput.Buy buy  = null;
        AlgorithmOutput.Sell sell = null;

        final List<Double> ema = new ArrayList<>(emaHistory);
        final double std = utils.Math.stdDev(ema);
        final double rsi = utils.Math.rsi(ema);
        final double ma  = utils.Math.average(ema);

        final double lower_band = ma - 4.0d * std * ma;

        // Buy
        if (rsi <= 30.0d && currentPrice <= lower_band) {
            if (lastInputArr.isEmpty()) {
                lastInputArr.add(currentPrice);
            } else if (utils.Math.average(new ArrayList<>(lastInputArr)) <= currentPrice) {
                
                final double confidence = utils.Math.clamp(((1.0d - std * 100.0d) + (100.0d - rsi) / 100.0d) / 2.0d, 0.0d, 0.3d);  // changing confidence has a massive effect on returns
                final long amount = (long) (allocatedToke * confidence / currentPrice);

                if(amount != 0L) buy = new AlgorithmOutput.Buy(amount);

            } else {
                lastInputArr.add(currentPrice);
                if (lastInputArr.size() > 5) lastInputArr.poll();
            }
        } else {
            lastInputArr.clear();
        }

        final double risk = utils.Math.clamp(std * 100d, 0.05d, 0.2d); // to put it into percentages

        // Sell
        final List<Pair<Bought, Long>> toBeSold = new ArrayList<>();

        // Trailing-profit logic
        for (final var item : holdings) {
            boolean isMarked = markedForSelling.contains(item);

            // Activate trailing if gained > risk
            if (!isMarked && currentPrice > item.price * (1.0d + risk)) {
                markedForSelling.add(item);
                trailingHigh.put(item, currentPrice);
                isMarked = true;
            }

            if (isMarked) {
                double high = trailingHigh.getOrDefault(item, currentPrice);

                // Update trailing high if still rising
                if (currentPrice > high) {
                    high = currentPrice;
                    trailingHigh.put(item, high);
                }

                // Sell if price falls more than risk from peak
                if (currentPrice < high * (1.0d - risk)) {
                    toBeSold.add(new Pair<>(item, item.amount));

                    // cleanup
                    markedForSelling.remove(item);
                    trailingHigh.remove(item);
                }
            }
        }

        // Stop-loss
        for (final var item : holdings) {
            if (currentPrice < item.price * (1.0d - risk * 2.0d)) {
                toBeSold.add(new Pair<>(item, item.amount));

                // cleanup
                markedForSelling.remove(item);
                trailingHigh.remove(item);
            }
        }

        if (!toBeSold.isEmpty()) sell = new AlgorithmOutput.Sell(toBeSold);
        return new AlgorithmOutput(buy, sell);
    }

    /*===========================================================*/

    @Override
    public void UpdateHistory(final History history) {
        final double alpha = 2.0d / (emaHistory.size() + 1.0d);
        final double last = emaHistory.peekLast();

        final double newEma = alpha * history.closingPrice + (1.0d - alpha) * last;

        emaHistory.pollFirst();
        emaHistory.addLast(newEma);
    }

    /*===========================================================*/
    /*===========================================================*/
    // Constructor(s)

    /**
     * Java equivalent of C++ Init::Init_EMA(q0, q1).
     * q0: first slidingWindow prices
     * q1: next slidingWindow prices
     */
    TACPP46(final Algorithm.Init init, final List<History> emaInit) {
        super(init);

        // SlidingWindow
        final int SW = 21;
        if (emaInit.size() < 2 * SW) throw new IllegalArgumentException("Init EMA: not enough history for Initialisation");
        
        final List<History> historyQ0;
        final List<History> historyQ1;
        switch(init) {

            case Algorithm.Init.TRADING: {

                final int n = emaInit.size();
                historyQ0 = List.copyOf(emaInit.subList(n - 2 * SW, n - SW));
                historyQ1 = List.copyOf(emaInit.subList(n - SW, n));
                emaInit.subList(n - 2 * SW, n).clear();

            } break;

            case Algorithm.Init.BACKTEST: {

                historyQ0 = List.copyOf(emaInit.subList(0, SW));
                historyQ1 = List.copyOf(emaInit.subList(SW, 2 * SW));
                emaInit.subList(0, 2 * SW).clear();

            } break;

            default: throw new IllegalArgumentException("Init");

        }

        final List<Double> q0 = historyQ0.stream().map(item -> item.closingPrice).toList();
        final List<Double> q1 = historyQ1.stream().map(item -> item.closingPrice).toList();

        final double alpha = 2.0d / (q1.size() + 1.0d);
        double ema = utils.Math.average(q0); // initial Value

        for (final double price : q1) {
            ema = alpha * price + (1.0d - alpha) * ema;
            emaHistory.add(ema);
        }

        if (emaHistory.isEmpty()) throw new IllegalStateException("EMA is Empty");
    }
}
