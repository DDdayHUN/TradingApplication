package utils;

import java.util.ArrayList;
import java.util.List;

/*===========================================================*/
/*===========================================================*/

public final class Math {
    /*===========================================================*/
    /*===========================================================*/
    // Constant(s)

    static private final long TRADING_DAYS = 252;

    /*===========================================================*/
    /*===========================================================*/
    // Public Method(es)

    /**
     * Computes the arithmetic mean of a list of numeric values.
     *
     * <p>The list must contain at least one element. The method does not modify
     * the input list.</p>
     *
     * @param list - A non-empty list of numeric values.
     * @return The arithmetic average of the list.
     * @throws IllegalArgumentException If the list is empty.
     */
    static public double average(final List<Double> list) {
        if(list.size() == 0) throw new IllegalArgumentException("Size");
        
        double sum = 0d;
        for(var item : list) sum += item;
        return sum / list.size();
    }

    /*===========================================================*/
    /**
     * Computes the standard deviation of returns derived from a price series.
     *
     * <p>This method converts each consecutive price pair into a simple return.
     * It then computes the sample standard deviation of those returns.</p>
     *
     * @param list - A list of prices in chronological order.
     * @return The standard deviation of returns.
     * @throws IllegalArgumentException If fewer than two prices are provided.
     */
    static public double stdDev(final List<Double> list) {
        if (list.size() < 2) throw new IllegalArgumentException("Size");

        // Compute returns
        final List<Double> returnsList = new ArrayList<>();
        for (int i = 1; i < list.size(); i++) {
            final double prev = list.get(i - 1);
            final double curr = list.get(i);
            final double r = (curr - prev) / prev;
            returnsList.add(r);
        }

        return java.lang.Math.sqrt(utils.Math.variance(returnsList));
    }

    /*===========================================================*/
    /**
     * Computes the sample variance of a list of numeric values.
     *
     * <p>This method uses the unbiased estimator, dividing by (n - 1). The list
     * must contain at least two elements.</p>
     *
     * @param list - A list of numeric values.
     * @return The sample variance of the list.
     * @throws IllegalArgumentException If the list contains fewer than two values.
     */
    static public double variance(final List<Double> list) {
        if(list.size() < 2) throw new IllegalArgumentException("Size");

        final double mean = utils.Math.average(list);

        double variance = 0d;
        for(var item : list) variance += java.lang.Math.pow(item - mean, 2);

        return variance / (list.size() - 1);
    }

    /*===========================================================*/
    /*
     * Special cases:
     *   If there are no gains, RSI = 0.
     *   If there are no losses, RSI = 100.
     *   If average loss is zero, RSI = 100.
     *   If average gain is zero, RSI = 0.
     */
    /**
     * Computes the Relative Strength Index (RSI) for a sequence of prices.
     *
     * <p>It uses simple averages of gains and losses {@code (not Wilder's smoothing)}. 
     * Consecutive increases contribute to the gain list, while decreases or equal 
     * values contribute to the loss list.</p>
     *
     * @param list - A list of prices in chronological order.
     * @return The RSI value in the range [0, 100].
     * @throws IllegalArgumentException If fewer than two prices are provided.
     */
    static public double rsi(final List<Double> list) {
        if (list.size() < 2) throw new IllegalArgumentException("RSI: size < 2");

        final List<Double> gaines = new ArrayList<>();
        final List<Double> losses = new ArrayList<>();

        // Collect gains and losses
        for (int i = 1; i < list.size(); i++) {
            final double prev = list.get(i - 1);
            final double curr = list.get(i);

            if (curr > prev) gaines.add(curr - prev);
            else losses.add(prev - curr);
        }

        if (gaines.isEmpty()) return 0.0d;
        if (losses.isEmpty()) return 100.0d;

        final double avgGain = Math.average(gaines);
        final double avgLoss = Math.average(losses);

        if (avgLoss == 0.0d) return 100.0d;
        if (avgGain == 0.0d) return 0.0d;

        final double rs = avgGain / avgLoss;
        
        return 100.0d - (100.0d / (1.0d + rs));
    }

    /*===========================================================*/
    /**
     * Computes the annualized Sharpe Ratio for a portfolio's capital history.
     *
     * <p>The input list must contain portfolio values (prices), not returns.
     * Returns are computed internally as percentage changes between consecutive
     * capital values.</p>
     *
     * @param capitalHistory    - A list of portfolio values over time.
     * @param riskFreeRate      - The annual risk-free rate (e.g., 0.02 for 2%).
     * @return The annualized Sharpe Ratio or NaN if it can't be computed.
     * @throws IllegalArgumentException If fewer than two capital values are provided.
     */
    static public double sharpeRatio(final List<Double> capitalHistory, final double riskFreeRate) {
        if (capitalHistory.size() < 2) throw new IllegalArgumentException("Size");

        // Compute returns
        final List<Double> returns = new ArrayList<>();
        for (int i = 1; i < capitalHistory.size(); i++) {
            final double r = (capitalHistory.get(i) / capitalHistory.get(i - 1)) - 1.0;
            returns.add(r);
        }

        final double meanReturn = Math.average(returns); // average of daily returns
        final double stdDev = Math.stdDev(capitalHistory); // standard deviation of daily returns

        if (stdDev == 0.0d) return Double.NaN;

        // Annualized Sharpe Ratio
        return (meanReturn * TRADING_DAYS - riskFreeRate) / (stdDev * java.lang.Math.sqrt(TRADING_DAYS));
    }

    /*===========================================================*/
    /**
     * Clamps a value to the inclusive range defined by two bounds.
     *
     * <p>The order of {@code clamp1} and {@code clamp2} does not matter; the
     * lower and upper bounds are determined automatically.</p>
     *
     * @param val       - The value to clamp.
     * @param clamp1    - One boundary of the clamp range.
     * @param clamp2    - The other boundary of the clamp range.
     * @return The clamped value.
     */
    static public double clamp(final double val, final double clamp1, final double clamp2) {
        final double min = java.lang.Math.min(clamp1, clamp2);
        final double max = java.lang.Math.max(clamp1, clamp2);
        return java.lang.Math.max(min, java.lang.Math.min(max, val));
    }

    /*===========================================================*/
    /*===========================================================*/
    // Private Method(es)

    private Math() {} // To prevent instantiation.
}
