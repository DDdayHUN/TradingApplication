package utils

import java.util.ArrayList
import kotlin.math.pow
import kotlin.math.sqrt

/*===========================================================*/
/*===========================================================*/

object Math {
    /*===========================================================*/
    /*===========================================================*/
    // Constant(s)

    private const val TRADING_DAYS: Long = 252

    /*===========================================================*/
    /*===========================================================*/
    // Public Method(es)
    /**
     * Computes the arithmetic mean of a list of numeric values.
     * 
     * The list must contain at least one element. The method does not modify
     * the input list.
     * 
     * @param list A non-empty list of numeric values.
     * @return The arithmetic average of the list.
     */
    fun average(list: List<Double>): Double {
        require(!list.isEmpty()) { "Size" }

        var sum = 0.0
        for (item in list) sum += item
        return sum / list.size
    }

    /*===========================================================*/
    /**
     * Computes the standard deviation of returns derived from a price series.
     * 
     * This method converts each consecutive price pair into a simple return.
     * It then computes the sample standard deviation of those returns.
     * 
     * @param list A list of prices in chronological order.
     * @return The standard deviation of returns.
     * @throws IllegalArgumentException If fewer than two prices are provided.
     */
    fun stdDev(list: List<Double>): Double {
        require(list.size >= 2) { "Size" }

        // Compute returns
        val returnsList: MutableList<Double> = ArrayList()
        for (i in 1..< list.size) {
            val prev: Double = list[i - 1]
            val curr: Double = list[i]
            val r = (curr - prev) / prev
            returnsList.add(r)
        }

        return sqrt(variance(returnsList))
    }

    /*===========================================================*/
    /**
     * Computes the sample variance of a list of numeric values.
     * 
     * 
     * This method uses the unbiased estimator, dividing by (n - 1). The list
     * must contain at least two elements.
     * 
     * @param list A list of numeric values.
     * @return The sample variance of the list.
     */
    fun variance(list: List<Double>): Double {
        require(list.size >= 2) { "Size" }

        val mean = average(list)

        var variance = 0.0
        for (item in list) variance += (item - mean).pow(2)

        return variance / (list.size - 1)
    }

    /*===========================================================*/ /*
     * Special cases:
     *   If there are no gains, RSI = 0.
     *   If there are no losses, RSI = 100.
     *   If average loss is zero, RSI = 100.
     *   If average gain is zero, RSI = 0.
     */
    /**
     * Computes the Relative Strength Index (RSI) for a sequence of prices.
     * 
     * 
     * It uses simple averages of gains and losses `(not Wilder's smoothing)`.
     * Consecutive increases contribute to the gain list, while decreases or equal
     * values contribute to the loss list.
     * 
     * @param list A list of prices in chronological order.
     * @return The RSI value in the range [0, 100].
     */
    fun rsi(list: List<Double>): Double {
        require(list.size >= 2) { "RSI: size < 2" }

        val gaines: MutableList<Double> = ArrayList()
        val losses: MutableList<Double> = ArrayList()

        // Collect gains and losses
        for (i in 1..<list.size) {
            val prev = list[i - 1]
            val curr = list[i]

            if (curr > prev) gaines.add(curr - prev)
            else losses.add(prev - curr)
        }

        if (gaines.isEmpty()) return 0.0
        if (losses.isEmpty()) return 100.0

        val avgGain = average(gaines)
        val avgLoss = average(losses)

        if (avgLoss == 0.0) return 100.0
        if (avgGain == 0.0) return 0.0

        val rs = avgGain / avgLoss

        return 100.0 - (100.0 / (1.0 + rs))
    }

    /*===========================================================*/
    /**
     * Computes the annualized Sharpe Ratio for a portfolio's capital history.
     * 
     * 
     * The input list must contain portfolio values (prices), not returns.
     * Returns are computed internally as percentage changes between consecutive
     * capital values.
     * 
     * @param capitalHistory A list of portfolio values over time.
     * @param riskFreeRate The annual risk-free rate (e.g., 0.02 for 2%).
     * @return The annualized Sharpe Ratio or NaN if it can't be computed.
     */
    fun sharpeRatio(capitalHistory: List<Double>, riskFreeRate: Double = 0.03): Double {
        require(capitalHistory.size >= 2) { "Size" }

        // Compute returns
        val returns: MutableList<Double> = ArrayList()
        for (i in 1..< capitalHistory.size) {
            val r = (capitalHistory[i] / capitalHistory[i - 1]) - 1.0
            returns.add(r)
        }

        val meanReturn = average(returns) // average of daily returns
        val stdDev = stdDev(capitalHistory) // standard deviation of daily returns

        if (stdDev == 0.0) return Double.NaN

        // Annualized Sharpe Ratio
        return (meanReturn * TRADING_DAYS - riskFreeRate) / (stdDev * sqrt(TRADING_DAYS.toDouble()))
    }
}
