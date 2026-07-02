package application.backtest

import application.signal.SignalGenerator
import domain.algorithm.TradingAlgorithm
import domain.signal.TradingSignal
import domain.assets.security.SecurityHistory
import domain.assets.security.SecurityHolding
import domain.tax.ITaxation
import kotlin.time.Instant

//===========================================================//
/**
 * AlgorithmBackTester is responsible for simulating and evaluating a trading m_Algorithm
 * over historical market data.
 * 
 * 
 * It runs a specified [TradingAlgorithm] over a defined time range for a given stock,
 * tracks virtual m_Holdings, capital changes, and performance metrics such as total trades and win rate.
 * 
 * 
 * The backtester supports both normal execution and debug execution, where additional
 * internal state (such as current m_Holdings) is printed for inspection.
 * 
 * 
 * This class is immutable in configuration (stock, range, initial capital, m_Algorithm m_Type),
 * but maintains mutable state during backtesting execution.
 */
//===========================================================//

class AlgorithmBackTester {
    //===========================================================//
    //===========================================================//
    // Private Field(s)

    private val m_StockName: String
    private val m_From: Instant
    private val m_To: Instant

    private val m_StartingCapital: Double
    private val m_Type: TradingAlgorithm.Type

    private val m_WithoutTax: BackTesterWithTaxationContext
    private val m_WithTax: BackTesterWithTaxationContext

    private val m_SignalGenerator: SignalGenerator
    private val m_Signlas: MutableList<TradingSignal>

    @Deprecated("This will be phased out") private val m_FromDEP: Int
    @Deprecated("This will be phased out") private val m_ToDEP: Int

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
        if(m_FromDEP != Int.MIN_VALUE && m_ToDEP != Int.MAX_VALUE) {
            m_WithoutTax.resetDEP()
            m_WithTax.resetDEP()
        }
        else {
            m_WithoutTax.reset()
            m_WithTax.reset()
        }
        m_WithoutTax.runBackTest()
        m_WithTax.runBackTest()
    }

    //===========================================================//

    private fun display(debug: Boolean) {
        println("#===============================================================#")
        println("# Algorithm back-tester")
        println("#===============================================================#")
        println("Stock: $m_StockName [$m_From-$m_To]")
        println("Starting Capital: " + String.format("%.2f", m_StartingCapital) + System.lineSeparator())
        m_WithoutTax.display()
        if (debug) m_WithoutTax.displayDebugInfo()
        m_WithTax.display()
        if (debug) m_WithTax.displayDebugInfo()
        if(debug) {
            println("#===============================================================#")
            println("Buy & Sale trades:")
            var counter: Long = 1
            for (signal in m_Signlas) {
                System.out.printf((counter++).toString() + " ")
                println(signal.formatToReadableText())
            }
        }
        println("#===============================================================#")
        println("#===============================================================#")
    }

    //===========================================================//
    //===========================================================//
    // Constructor(s)

    @Deprecated("This will get phased out in favor of the other constructor")
    constructor(taxation: ITaxation, type: TradingAlgorithm.Type, startingCapital: Double, stockName: String, from: Int, to: Int) {
        require(startingCapital >= 0) { "Capital" }

        m_StockName = stockName
        m_FromDEP = from
        m_ToDEP = to

        m_StartingCapital = startingCapital
        m_Type = type

        m_WithoutTax = BackTesterWithTaxationContext(null, TradingAlgorithm.initForBackTest(m_Type, m_StockName, m_FromDEP, m_ToDEP))
        m_WithTax = BackTesterWithTaxationContext(taxation, TradingAlgorithm.initForBackTest(m_Type, m_StockName, m_FromDEP, m_ToDEP))

        m_SignalGenerator = SignalGenerator()
        m_Signlas = ArrayList()

        m_From = Instant.DISTANT_PAST
        m_To = Instant.DISTANT_FUTURE
    }

    constructor(taxation: ITaxation, type: TradingAlgorithm.Type, startingCapital: Double, stockName: String, from: Instant, to: Instant) {
        require(startingCapital >= 0) { "Capital" }

        m_StockName = stockName
        m_From = from
        m_To = to

        m_StartingCapital = startingCapital
        m_Type = type

        m_WithoutTax = BackTesterWithTaxationContext(null, TradingAlgorithm.initForBackTest(m_Type, m_StockName, m_From, m_To))
        m_WithTax = BackTesterWithTaxationContext(taxation, TradingAlgorithm.initForBackTest(m_Type, m_StockName, m_From, m_To))

        m_SignalGenerator = SignalGenerator()
        m_Signlas = ArrayList()

        m_FromDEP = Int.MIN_VALUE
        m_ToDEP = Int.MAX_VALUE
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
                m_TradingAlgorithm.updateHistory(history)
            }
        }

        //===========================================================//

        @Deprecated("This will get phased out in favor of the reset function")
        fun resetDEP() {
            val pair = TradingAlgorithm.initForBackTest(m_Type, m_StockName, m_FromDEP, m_ToDEP)

            m_TradingAlgorithm = pair.second
            m_HistoryWeRunAgainst = pair.first

            m_Holdings.clear()
            m_CapitalHistory.clear()

            m_CurrentCapital = m_StartingCapital
            m_TotalSellsMade = 0
            m_WinningTrades = 0
        }

        fun reset() {
            val pair = TradingAlgorithm.initForBackTest(m_Type, m_StockName, m_From, m_To)

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
            require(!m_CapitalHistory.isEmpty()) { "m_CapitalHistory is empty" }

            val last = m_CapitalHistory.last()
            val profit = last - m_StartingCapital
            val percent = (profit / m_StartingCapital) * 100.0

            val winRate: Double
            if (m_TotalSellsMade <= 0) winRate = Double.NaN
            else winRate = m_WinningTrades * 100.0 / m_TotalSellsMade

            if (m_Taxation != null) println("With Taxes:")
            else println("Without Taxes:")

            println("    Profit: " + String.format("%.2f", profit))
            println("    Return: " + String.format("%.2f", percent) + "%")
            println()

            println("    Total Sells Made: $m_TotalSellsMade")
            println("    Winrate: " + String.format("%.2f", winRate) + "%")
            println(
                "    Sharpe Ratio: " + String.format(
                    "%.2f",
                    utils.Math.sharpeRatio(m_CapitalHistory, 0.03)
                )
            )
            println()
        }

        //===========================================================//

        fun displayDebugInfo() {
            println("    DEBUG:")
            print("  Holding: ")
            if (m_Holdings.isEmpty()) println("None")
            else {
                println()
                for (item in m_Holdings) println("        Entry Price: " + String.format("%.2f", item.entryPrice) + " db: " + item.amount)
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

            val signals = m_SignalGenerator.createSignal(
                m_StockName,
                ret,
                m_CurrentCapital,
                currentPrice,
                projectedStockCount
            )

            signals.stream()
                .filter { it.action != TradingSignal.Action.HOLD }
                .forEach { m_Signlas.add(it) }

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
        }
    }
}
