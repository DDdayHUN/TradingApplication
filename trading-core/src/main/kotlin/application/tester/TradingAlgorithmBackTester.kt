package application.tester

import domain.algorithm.ITradingAlgorithm
import domain.algorithm.TradingAlgorithm
import domain.market.security.SecurityHistory
import domain.market.security.SecurityHolding
import domain.market.security.SecurityIdentifier
import domain.tax.ITaxation
import domain.tax.Taxation
import format
import kotlin.math.max
import kotlin.math.pow
import kotlin.time.Instant
import kotlin.time.toJavaInstant

//===========================================================//
/**
 * AlgorithmBackTester is responsible for simulating and evaluating a trading m_Algorithm
 * over historical market data.
 * 
 * It runs a specified [TradingAlgorithm] over a defined time range for a given stock,
 * tracks virtual m_Holdings, capital changes, and performance metrics such as total trades and win rate.
 * 
 * The back tester supports both normal execution and debug execution, where additional
 * internal state (such as current m_Holdings) is printed for inspection.
 * 
 * This class is immutable in configuration (stock, range, initial capital, m_Algorithm, m_TradingAlgorithmType)
 * but maintains mutable state during backtesting execution.
 */
//===========================================================//

class TradingAlgorithmBackTester {
    //===========================================================//
    //===========================================================//
    // Private Field(s)

    private val m_SecurityIdentifier: SecurityIdentifier
    private val m_From: Instant
    private val m_To: Instant

    private val m_StartingCapital: Double

    private val m_TaxationType: Taxation.Type?
    private var m_Taxation: ITaxation?

    private val m_TradingAlgorithmType: TradingAlgorithm.Type
    private var m_TradingAlgorithm: ITradingAlgorithm
    private var m_HistoryWeRunAgainst: List<SecurityHistory>

    private val m_Holdings: MutableList<SecurityHolding>
    private val m_CapitalHistory: MutableList<Double>

    private var m_CurrentCapital: Double
    private var m_TotalBuysMade: Int
    private var m_TotalSellsMade: Int
    private var m_WinningTrades: Int
    private var m_ForceClosedTrades: Int

    //===========================================================//
    //===========================================================//
    // Public Method(es)

    fun runBackTest(): Output {
        reset()
        return internalRunBackTest()
    }

    //===========================================================//
    //===========================================================//
    // Private Method(es)

    private fun reset() {
        val pair = TradingAlgorithm.create(m_TradingAlgorithmType, m_SecurityIdentifier, m_From, m_To)

        if(m_TaxationType != null) m_Taxation = Taxation.create(m_TaxationType)

        m_TradingAlgorithm = pair.second
        m_HistoryWeRunAgainst = pair.first

        m_Holdings.clear()
        m_CapitalHistory.clear()

        m_CurrentCapital = m_StartingCapital
        m_TotalBuysMade = 0
        m_TotalSellsMade = 0
        m_WinningTrades = 0
        m_ForceClosedTrades = 0
    }

    //===========================================================//

    private fun internalRunBackTest(): Output {
        for (history in m_HistoryWeRunAgainst) {
            val currentPrice = history.closingPrice
            runOneIteration(currentPrice)
        }

        forceSell()

        val winRate =
            if (m_TotalSellsMade <= 0) Double.NaN
            else (m_WinningTrades.toDouble() / m_TotalSellsMade.toDouble())

        var peak = m_CapitalHistory.first()
        var maxDrawdown = 0.0

        m_CapitalHistory.forEach {
            peak = max(peak, it)
            val drawdown = (peak - it) / peak
            maxDrawdown = max(maxDrawdown, drawdown)
        }

        return Output(
            m_TradingAlgorithmType,
            m_SecurityIdentifier,
            m_TaxationType,
            m_StartingCapital,
            m_CapitalHistory.last(),
            m_From,
            m_To,
            m_TotalBuysMade,
            m_TotalSellsMade,
            m_ForceClosedTrades,
            winRate,
            maxDrawdown,
            domain.utils.Math.sharpeRatio(m_CapitalHistory)
        )
    }

    //===========================================================//

    private fun runOneIteration(currentPrice: Double) {
        val ret = m_TradingAlgorithm.run(m_Holdings, m_CurrentCapital, currentPrice)

        if (ret.buy != null) {
            m_CurrentCapital -= ret.buy.amount * currentPrice
            check(m_CurrentCapital > 0.0)

            m_Holdings.add(SecurityHolding(currentPrice, ret.buy.amount))
            m_TotalBuysMade++
        }

        if (ret.sell != null) {
            for (item in ret.sell.batches) {
                val bought = item.first
                val amount = item.second

                require(amount <= bought.amount) { "Sell Amount" }

                m_Holdings.remove(bought)

                if (m_Taxation == null) m_CurrentCapital += amount * currentPrice
                else {
                    val revenue = amount * currentPrice
                    val costBasis = amount * bought.entryPrice
                    m_CurrentCapital += m_Taxation!!.calculateRevenueAfterTax(revenue, costBasis)
                }

                if (amount != bought.amount) m_Holdings.add(
                    SecurityHolding(
                        bought.entryPrice,
                        bought.amount - amount
                    )
                )

                m_TotalSellsMade++
                if (currentPrice > bought.entryPrice) m_WinningTrades++
            }
        }

        var sum = 0.0
        for (item in m_Holdings) sum += (currentPrice * item.amount)
        m_CapitalHistory.add(m_CurrentCapital + sum)
    }

    //===========================================================//

    private fun forceSell() {
        val lastPrice = m_HistoryWeRunAgainst.last().closingPrice
        for(holding in m_Holdings) {
            if (m_Taxation == null) m_CurrentCapital += holding.amount * lastPrice
            else {
                val revenue = holding.amount * lastPrice
                val costBasis = holding.amount * holding.entryPrice
                m_CurrentCapital += m_Taxation!!.calculateRevenueAfterTax(revenue, costBasis)
            }
            m_TotalSellsMade++
            if (lastPrice > holding.entryPrice) m_WinningTrades++
            m_ForceClosedTrades++
        }
    }

    //===========================================================//
    //===========================================================//
    // Constructor(s)

    constructor(
        type: TradingAlgorithm.Type,
        securityIdentifier: SecurityIdentifier,
        startingCapital: Double,
        taxation: Taxation.Type? = null,
        from: Instant = Instant.DISTANT_PAST,
        to: Instant = Instant.DISTANT_FUTURE,
    ) {
        require(startingCapital >= 0) { "Capital" }

        m_SecurityIdentifier = securityIdentifier
        m_From = from
        m_To = to

        m_StartingCapital = startingCapital
        m_TaxationType = taxation
        m_Taxation = if(m_TaxationType != null) Taxation.create(m_TaxationType) else null

        m_TradingAlgorithmType = type
        val pair = TradingAlgorithm.create(m_TradingAlgorithmType, securityIdentifier, m_From, m_To)
        m_TradingAlgorithm = pair.second
        m_HistoryWeRunAgainst = pair.first

        m_Holdings = ArrayList()
        m_CapitalHistory = ArrayList()

        m_CurrentCapital = m_StartingCapital
        m_TotalBuysMade = 0
        m_TotalSellsMade = 0
        m_WinningTrades = 0
        m_ForceClosedTrades = 0
    }

    //===========================================================//
    //===========================================================//
    // Helper Class(es)

    data class Output(
        val tradingAlgorithmType: TradingAlgorithm.Type,
        val securityIdentifier: SecurityIdentifier,
        val taxation: Taxation.Type?,
        val startingCapital: Double,
        val totalCapital: Double,
        val from: Instant,
        val to: Instant,
        val totalBuysMade: Int,
        val totalSellsMade: Int,
        val forceClosedTrades: Int,
        val tradeWinrate: Double,
        val maxDrawdown: Double,
        val sharpeRatio: Double
    ) {
        fun display() {
            val tax = if(taxation != null) "With" else "Without"

            val zone = java.time.ZoneId.systemDefault()
            val formatter = java.time.format.DateTimeFormatter.ofPattern("yyyy.MM.dd")

            val fromDate = from.toJavaInstant()
            val toDate = to.toJavaInstant()

            val deltaCapital = totalCapital - startingCapital
            val deltaCapitalInPercent = (deltaCapital / startingCapital) * 100.0

            val years = java.time.Duration.between(fromDate, toDate).toDays().toDouble() / 365.2425
            val cagr = ((totalCapital / startingCapital).pow(1.0 / years) - 1.0) * 100.0

            val calmar = cagr / maxDrawdown

            println("#===============================================================#")
            println("# Algorithm Backtesting | Algorithm: $tradingAlgorithmType")
            println("#===============================================================#")
            println("Stock: ${securityIdentifier.tickerSymbol} [${fromDate.atZone(zone).format(formatter)}-${toDate.atZone(zone).format(formatter)}]")
            println("Starting Capital: ${startingCapital.format(2)}")
            println("Taxation: $tax")
            println()

            val padding = 24
            println("| ${"Metric".padEnd(24)} | ${"Value".padStart(padding)} |")
            println("-".repeat(50))

            println("| ${"Total Capital".padEnd(padding)} | ${totalCapital.format(2).padStart(padding)} |")
            println("| ${"Delta Capital".padEnd(padding)} | ${deltaCapital.format(2).padStart(padding)} |")
            println("| ${"Percent Change".padEnd(padding)} | ${(deltaCapitalInPercent.format(2) + "%").padStart(padding)} |")
            println("| ${"CAGR".padEnd(padding)} | ${(cagr.format(2) + "%").padStart(padding)} |")
            println("| ${"Total Buys".padEnd(padding)} | ${totalBuysMade.toString().padStart(padding)} |")
            println("| ${"Total Sells".padEnd(padding)} | ${totalSellsMade.toString().padStart(padding)} |")
            println("| ${"Forced Closures".padEnd(padding)} | ${forceClosedTrades.toString().padStart(padding)} |")
            println("| ${"Winrate".padEnd(padding)} | ${(tradeWinrate.times(100.0).format(2) + "%").padStart(padding)} |")
            println("| ${"Max Drawdown".padEnd(padding)} | ${(maxDrawdown.times(100.0).format(2) + "%").padStart(padding)} |")
            println("| ${"Sharpe Ratio".padEnd(padding)} | ${sharpeRatio.format(2).padStart(padding)} |")
            println("| ${"Calmar Ratio".padEnd(padding)} | ${calmar.format(2).padStart(padding)} |")
            println()
        }
    }
}
