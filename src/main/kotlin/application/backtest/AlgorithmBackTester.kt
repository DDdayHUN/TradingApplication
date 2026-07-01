package application.backtest

import application.signal.SignalGenerator
import domain.algorithm.Algorithm
import domain.signal.TradingSignal
import domain.stock.History
import domain.stock.Holding
import domain.tax.Taxation

//===========================================================//
/**
 * AlgorithmBackTester is responsible for simulating and evaluating a trading m_Algorithm
 * over historical market data.
 * 
 * 
 * It runs a specified [Algorithm] over a defined time range for a given stock,
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

class AlgorithmBackTester(
    taxation: Taxation,
    type: Algorithm.Type,
    startingCapital: Double,
    stockName: String,
    from: Int,
    to: Int
) {
    //===========================================================//
    //===========================================================//
    // Private Field(s)

    private val m_StockName: String
    private val m_From: Int
    private val m_To: Int

    private val m_StartingCapital: Double
    private val m_Type: Algorithm.Type

    private val m_WithoutTax: BackTesterWithTaxationContext
    private val m_WithTax: BackTesterWithTaxationContext

    private val m_SignalGenerator: SignalGenerator
    private val m_Signlas: MutableList<TradingSignal>

    //===========================================================//
    //===========================================================//
    // Public Method(es)

    fun runBackTest() {
        m_WithoutTax.reset(Algorithm.initForBackTest(m_Type, m_StockName, m_From, m_To))
        m_WithTax.reset(Algorithm.initForBackTest(m_Type, m_StockName, m_From, m_To))
        m_WithoutTax.runBackTest()
        m_WithTax.runBackTest()
        display(false)
    }

    //===========================================================//

    fun runBackTestWithDebug() {
        m_WithoutTax.reset(Algorithm.initForBackTest(m_Type, m_StockName, m_From, m_To))
        m_WithTax.reset(Algorithm.initForBackTest(m_Type, m_StockName, m_From, m_To))
        m_WithoutTax.runBackTest()
        m_WithTax.runBackTest()
        display(true)
    }

    //===========================================================//
    //===========================================================//
    // Private Method(es)

    private fun display(debug: Boolean) {
        println("#===============================================================#")
        println(System.lineSeparator() + "### Algorithm back-tester ###" + System.lineSeparator())
        println("#===============================================================#")
        println("Buy & Sale trades:")
        var counter: Long = 1
        for (signal in m_Signlas) {
            System.out.printf((counter++).toString() + " ")
            println(signal.formatToReadableText())
        }
        println(System.lineSeparator())
        println("#===============================================================#")
        println(System.lineSeparator())
        println("Stock: $m_StockName [$m_From-$m_To]")
        println("Starting Capital: " + String.format("%.2f", m_StartingCapital) + System.lineSeparator())
        m_WithoutTax.display()
        if (debug) m_WithoutTax.displayDebugInfo()
        m_WithTax.display()
        if (debug) m_WithTax.displayDebugInfo()
        println("#===============================================================#")
    }

    //===========================================================//
    //===========================================================//
    // Constructor(s)

    init {
        require(startingCapital >= 0) { "Capital" }

        m_StockName = stockName
        m_From = from
        m_To = to

        m_StartingCapital = startingCapital
        m_Type = type

        m_WithoutTax = BackTesterWithTaxationContext(null, Algorithm.initForBackTest(m_Type, m_StockName, m_From, m_To))
        m_WithTax = BackTesterWithTaxationContext(taxation, Algorithm.initForBackTest(m_Type, m_StockName, m_From, m_To))

        m_SignalGenerator = SignalGenerator()
        m_Signlas = ArrayList()
    }

    //===========================================================//
    //===========================================================//
    // Helper Class(es)

    private inner class BackTesterWithTaxationContext(
        taxation: Taxation?,
        pair: Pair<List<History>, Algorithm>
    ) {
        //===========================================================//
        //===========================================================//
        // Private Field(s)

        private val m_Taxation: Taxation?

        private var m_Algorithm: Algorithm
        private var m_HistoryWeRunAgainst: List<History>

        private val m_Holdings: MutableList<Holding>
        private val m_CapitalHistory: MutableList<Double>

        private var m_CurrentCapital: Double

        private var m_TotalSellsMade: Long = 0
        private var m_WinningTrades: Long = 0

        //===========================================================//
        //===========================================================//
        // Public Method(es)
        fun runBackTest() {
            for (history in m_HistoryWeRunAgainst) {
                val currentPrice = history.closingPrice
                runOneIteration(currentPrice)
                m_Algorithm.updateHistory(history)
            }
        }

        //===========================================================//
        fun reset(pair: Pair<List<History>, Algorithm>) {
            m_Algorithm = pair.second
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

        fun runOneIteration(currentPrice: Double) {
            val ret = m_Algorithm.run(m_Holdings, m_CurrentCapital, currentPrice)

            var projectedStockCount = currentStockCount
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
                m_Holdings.add(Holding(currentPrice, ret.buy.amount))
            }

            if (ret.sell != null) {
                for (item in ret.sell.batches) {
                    val bought = item.first
                    val amount = item.second

                    check(amount <= bought.amount) { "Sell Amount" }

                    m_Holdings.remove(bought)

                    if (m_Taxation == null) m_CurrentCapital += amount * currentPrice
                    else {
                        val revenue: Double = amount * currentPrice
                        val costBasis: Double = amount * bought.entryPrice
                        m_CurrentCapital += m_Taxation.calculateRevenueAfterTax(revenue, costBasis)
                    }

                    if (amount != bought.amount) m_Holdings.add(
                        Holding(
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

        val currentStockCount: Long
            //===========================================================//
            get() {
                var count = 0L

                for (holding in m_Holdings) {
                    count += holding.amount
                }

                return count
            }

        //===========================================================//

        fun getSellAmount(sell: Algorithm.Output.Sell): Long {
            var amount = 0L
            for (batch in sell.batches) {
                amount += batch.second
            }

            return amount
        }

        //===========================================================//
        //===========================================================//
        // Constructor(s)

        init {
            m_Taxation = taxation
            m_CurrentCapital = m_StartingCapital

            m_Algorithm = pair.second

            m_HistoryWeRunAgainst = pair.first
            m_Holdings = ArrayList()
            m_CapitalHistory = ArrayList()
        }
    }
}
