package domain.utils

import java.util.ArrayList
import kotlin.math.pow
import kotlin.math.sqrt

/*===========================================================*/
/*===========================================================*/

object Math {
    /*===========================================================*/
    /*===========================================================*/
    // Constant(s)

    private const val TRADING_DAYS: Int = 252

    /*===========================================================*/
    /*===========================================================*/
    // Public Method(es)
    /**
     * Computes the standard deviation of returns derived from a price series.
     * The list must contain prices in chronological order.
     * 
     * This method converts each consecutive price pair into a simple return.
     * It then computes the sample standard deviation of those returns.
     *
     * @return The standard deviation of returns.
     * @throws IllegalArgumentException If fewer than two prices are provided.
     */
    fun List<Double>.stdDev(): Double {
        require(this.size >= 2) { "Size" }

        // Compute returns
        val returnsList: MutableList<Double> = ArrayList()
        for (i in 1..< this.size) {
            val prev: Double = this[i - 1]
            val curr: Double = this[i]
            val r = (curr - prev) / prev
            returnsList.add(r)
        }

        return sqrt(returnsList.variance())
    }

    /*===========================================================*/
    /**
     * Computes the sample variance of a list of numeric values.
     * 
     * This method uses the unbiased estimator, dividing by (n - 1). The list
     * must contain at least two elements.
     *
     * @return The sample variance of the list.
     */
    fun List<Double>.variance(): Double {
        require(this.size >= 2) { "Size" }

        val mean = this.average()

        var variance = 0.0
        for (item in this) variance += (item - mean).pow(2)

        return variance / (this.size - 1)
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
     * The list must contain prices in chronological order.
     * 
     * It uses simple averages of gains and losses `(not Wilder's smoothing)`.
     * Consecutive increases contribute to the gain list, while decreases or equal
     * values contribute to the loss list.
     *
     * @return The RSI value in the range [0, 100].
     */
    fun List<Double>.rsi(): Double {
        require(this.size >= 2) { "Size" }

        val gaines: MutableList<Double> = ArrayList()
        val losses: MutableList<Double> = ArrayList()

        // Collect gains and losses
        for (i in 1..<this.size) {
            val prev = this[i - 1]
            val curr = this[i]

            if (curr > prev) gaines.add(curr - prev)
            else losses.add(prev - curr)
        }

        if (gaines.isEmpty()) return 0.0
        if (losses.isEmpty()) return 100.0

        val avgGain = gaines.average()
        val avgLoss = losses.average()

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

        val meanReturn = returns.average() // average of daily returns
        val stdDev = capitalHistory.stdDev() // standard deviation of daily returns TODO: Ehelyett nem retruns.stdDev() kéne???

        if (stdDev == 0.0) return Double.NaN

        // Annualized Sharpe Ratio
        return (meanReturn * TRADING_DAYS - riskFreeRate) / (stdDev * sqrt(TRADING_DAYS.toDouble()))
    }

    /*===========================================================*/
    /**
     * Returns the median value of the list.
     *
     * The median is the middle value after sorting the list.
     * For an even-sized list, it is the average of the two middle values.
     *
     * @return The median value, or NaN if the list is empty.
     */
    fun List<Double>.median(): Double {
        if (isEmpty()) return Double.NaN

        val sorted = sorted()
        val middle = sorted.size / 2

        return if (sorted.size % 2 == 0) (sorted[middle - 1] + sorted[middle]) / 2.0
        else sorted[middle]
    }

    /*===========================================================*/
    /**
     * Removes a percentage of values from both ends of the list.
     *
     * The list is sorted first. The lowest `percent` and highest `percent`
     * of values are removed.
     *
     * Example:
     *   [1, 2, 3, 4, 5], trim(0.2) -> [2, 3, 4]
     *
     * @param percent Fraction of values to remove from each end. 0.2 means 20% from the bottom and 20% from the top.
     * @return A sorted list with the specified extremes removed.
     */
    fun List<Double>.trim(percent: Double): List<Double> {
        if (isEmpty()) return listOf()

        val sorted = sorted()
        val removeCount = (size * percent).toInt()

        return sorted
            .drop(removeCount)
            .dropLast(removeCount)
    }

    /*===========================================================*/
    /**
     * Returns the bottom percentage of values in the list.
     *
     * Example:
     *   [1, 2, 3, 4, 5], bottom(0.4) -> [1, 2]
     *
     * @param percent Fraction of values to return. 0.4 means the lowest 40% of values.
     * @return The lowest `percent` of values, sorted in ascending order.
     */
    fun List<Double>.bottom(percent: Double): List<Double> {
        if (isEmpty()) return listOf()

        val sorted = sorted()
        val removeCount = (size * percent).toInt()

        return sorted.take(removeCount)
    }

    /*===========================================================*/
    /**
     * Returns the top percentage of values in the list.
     *
     * Example:
     *   [1, 2, 3, 4, 5], top(0.4) -> [4, 5]
     *
     * @param percent Fraction of values to return. 0.4 means the highest 40% of values.
     * @return The highest `percent` of values, sorted in ascending order.
     */
    fun List<Double>.top(percent: Double): List<Double> {
        if (isEmpty()) return listOf()

        val sorted = sorted()
        val removeCount = (size * percent).toInt()

        return sorted.takeLast(removeCount)
    }
}
