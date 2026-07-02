package application.backtest

import domain.algorithm.TradingAlgorithm
import domain.signal.TradingSignal
import domain.assets.security.SecurityHistory
import domain.assets.security.SecurityHolding
import domain.assets.security.SecurityIdentifier
import domain.tax.ITaxation
import kotlin.time.Instant

//===========================================================//
/**
 * AlgorithmBackTester is responsible for simulating and evaluating a trading m_Algorithm
 * over historical market data.
 * 
 * It runs a specified [TradingAlgorithm] over a defined time range for a given stock,
 * tracks virtual m_Holdings, capital changes, and performance metrics such as total trades and win rate.
 * 
 * The backtester supports both normal execution and debug execution, where additional
 * internal state (such as current m_Holdings) is printed for inspection.
 * 
 * This class is immutable in configuration (stock, range, initial capital, m_Algorithm m_Type),
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
    private val m_Type: TradingAlgorithm.Type

    private val m_WithoutTax: BackTesterWithTaxationContext
    private val m_WithTax: BackTesterWithTaxationContext

    //===========================================================//
    //===========================================================//
    // Public Method(es)

    fun runBackTest() {
        internalRunBackTest()
        display(false)
    }

    //===========================================================//

    fun runBackTestWithDebug() {
        internalRunBackTest()
        display(true)
    }

    //===========================================================//
    //===========================================================//
    // Private Method(es)

    private fun internalRunBackTest() {
        m_WithoutTax.reset()
        m_WithTax.reset()
        m_WithoutTax.runBackTest()
        m_WithTax.runBackTest()
    }

    //===========================================================//

    private fun display(debug: Boolean) {
        println("#===============================================================#")
        println("# Algorithm back-tester")
        println("#===============================================================#")
        val zone = java.time.ZoneId.systemDefault()
        val formatter = java.time.format.DateTimeFormatter.ofPattern("yyyy.MM.dd")
        println("Stock: ${m_SecurityIdentifier.name} " +
                "[" +
                "${java.time.Instant.ofEpochMilli(m_From.toEpochMilliseconds()).atZone(zone).format(formatter)}" +
                "-" +
                "${java.time.Instant.ofEpochMilli(m_To.toEpochMilliseconds()).atZone(zone).format(formatter)}" +
                "]"
        )
        println("Starting Capital: " + String.format("%.2f", m_StartingCapital) + System.lineSeparator())
        m_WithoutTax.display()
        m_WithTax.display()
        if (debug) {
            m_WithoutTax.displayDebugInfo()
            m_WithTax.displayDebugInfo()
        }
        println("#===============================================================#")
        println("#===============================================================#")
    }

    //===========================================================//
    //===========================================================//
    // Constructor(s)

    constructor(taxation: ITaxation, type: TradingAlgorithm.Type, securityIdentifier: SecurityIdentifier, startingCapital: Double, from: Instant, to: Instant) {
        require(startingCapital >= 0) { "Capital" }

        m_SecurityIdentifier = securityIdentifier
        m_From = from
        m_To = to

        m_StartingCapital = startingCapital
        m_Type = type

        m_WithoutTax = BackTesterWithTaxationContext(null, TradingAlgorithm.create(m_Type, securityIdentifier, m_From, m_To))
        m_WithTax = BackTesterWithTaxationContext(taxation, TradingAlgorithm.create(m_Type, securityIdentifier, m_From, m_To))
    }

    //===========================================================//
    //===========================================================//
    // Helper Class(es)

    private inner class BackTesterWithTaxationContext {
        //===========================================================//
        //===========================================================//
        // Private Field(s)

        private val m_Taxation: ITaxation?

        private var m_TradingAlgorithm: TradingAlgorithm
        private var m_HistoryWeRunAgainst: List<SecurityHistory>

        private val m_Holdings: MutableList<SecurityHolding>
        private val m_CapitalHistory: MutableList<Double>

        private var m_CurrentCapital: Double

        private var m_TotalSellsMade: Long = 0
        private var m_WinningTrades: Long = 0

        private val m_Signlas: MutableList<TradingSignal>

        private val m_CurrentStockCount: Long
            get() {
                var count = 0L
                for (holding in m_Holdings) count += holding.amount
                return count
            }

        //===========================================================//
        //===========================================================//
        // Public Method(es)

        fun runBackTest() {
            for (history in m_HistoryWeRunAgainst) {
                val currentPrice = history.closingPrice
                runOneIteration(currentPrice)
            }
        }

        //===========================================================//

        fun reset() {
            val pair = TradingAlgorithm.create(m_Type, m_SecurityIdentifier, m_From, m_To)

            m_TradingAlgorithm = pair.second
            m_HistoryWeRunAgainst = pair.first

            m_Holdings.clear()
            m_CapitalHistory.clear()

            m_CurrentCapital = m_StartingCapital
            m_TotalSellsMade = 0
            m_WinningTrades = 0
        }

        //===========================================================//

        fun display() {
            require(!m_CapitalHistory.isEmpty()) { "CapitalHistory is empty" }

            val last = m_CapitalHistory.last()
            val deltaCapital = last - m_StartingCapital
            val deltaCapitalInPercent = (deltaCapital / m_StartingCapital) * 100.0
            val winRate = if (m_TotalSellsMade <= 0) Double.NaN else (m_WinningTrades * 100.0 / m_TotalSellsMade)

            if (m_Taxation != null) println("# With taxes on trades: ")
            else println("# Without taxes on trades: ")

            println("    Total Capital: " + String.format("%.2f", last))
            println("    Delta Capital: " + String.format("%.2f", deltaCapital))
            println("    Percent change: " + String.format("%.2f", deltaCapitalInPercent) + "%")
            println()

            println("    Total Sells Made: $m_TotalSellsMade")
            println("    Winrate: " + String.format("%.2f", winRate) + "%")
            println("    Sharpe Ratio: " + String.format("%.2f", utils.Math.sharpeRatio(m_CapitalHistory, 0.03)))
            println()
        }

        //===========================================================//

        fun displayDebugInfo() {
            println("#===============================================================#")
            if (m_Taxation != null) print("# With taxes on trades: ")
            else print("# Without taxes on trades: ")
            println("DEBUG_INFO")
            print("  Holding: ")
            if (m_Holdings.isEmpty()) println("None")
            else {
                println()
                for (item in m_Holdings) println("        Entry Price: " + String.format("%.2f", item.entryPrice) + " db: " + item.amount)
            }
            print("  Buy & Sale trades:")
            if(m_Signlas.isEmpty()) println("None")
            else {
                println()
                var counter = 1L
                for (signal in m_Signlas) {
                    print("        " + (counter++).toString() + " " + signal.formatToReadableText())
                    println()
                }
            }
        }

        //===========================================================//
        //===========================================================//
        // Private Method(es)

        private fun runOneIteration(currentPrice: Double) {
            val ret = m_TradingAlgorithm.run(m_Holdings, m_CurrentCapital, currentPrice)

            var projectedStockCount = m_CurrentStockCount
            if (ret.buy != null) projectedStockCount += ret.buy.amount
            if (ret.sell != null) projectedStockCount -= getSellAmount(ret.sell)

            m_Signlas.add(TradingSignal(
                ret,
                currentPrice
            ))

            if (ret.buy != null) {
                m_CurrentCapital -= ret.buy.amount * currentPrice
                m_Holdings.add(SecurityHolding(currentPrice, ret.buy.amount))
            }

            if (ret.sell != null) {
                for (item in ret.sell.batches) {
                    val bought = item.first
                    val amount = item.second

                    check(amount <= bought.amount) { "Sell Amount" }

                    m_Holdings.remove(bought)

                    if (m_Taxation == null) m_CurrentCapital += amount * currentPrice
                    else {
                        val revenue = amount * currentPrice
                        val costBasis = amount * bought.entryPrice
                        m_CurrentCapital += m_Taxation.calculateRevenueAfterTax(revenue, costBasis)
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

        private fun getSellAmount(sell: TradingAlgorithm.Output.Sell): Long {
            var amount = 0L
            for (batch in sell.batches) {
                amount += batch.second
            }

            return amount
        }

        //===========================================================//
        //===========================================================//
        // Constructor(s)

        constructor(taxation: ITaxation?, pair: Pair<List<SecurityHistory>, TradingAlgorithm>) {
            m_Taxation = taxation
            m_CurrentCapital = m_StartingCapital

            m_TradingAlgorithm = pair.second

            m_HistoryWeRunAgainst = pair.first
            m_Holdings = ArrayList()
            m_CapitalHistory = ArrayList()

            m_Signlas = ArrayList()
        }
    }
}
