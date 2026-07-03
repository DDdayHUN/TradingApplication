package application.tester

import domain.algorithm.ITradingAlgorithm
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
    private val m_Taxation: ITaxation?

    private val m_Type: TradingAlgorithm.Type
    private var m_TradingAlgorithm: ITradingAlgorithm
    private var m_HistoryWeRunAgainst: List<SecurityHistory>

    private val m_Holdings: MutableList<SecurityHolding>
    private val m_CapitalHistory: MutableList<Double>
    private val m_Signlas: MutableList<TradingSignal>

    private var m_CurrentCapital: Double
    private var m_TotalBuysMade: Long
    private var m_TotalSellsMade: Long
    private var m_WinningTrades: Long

    //===========================================================//
    //===========================================================//
    // Public Method(es)

    fun runBackTest(display: DisplayMode = DisplayMode.NoDisplay): Output {
        reset()
        val ret = internalRunBackTest()
        if(display is DisplayMode.Display) display(display.debug)
        return ret;
    }

    //===========================================================//
    //===========================================================//
    // Private Method(es)

    private fun display(debug: DebugMode) {
        require(!m_CapitalHistory.isEmpty()) { "CapitalHistory is empty" }

        val last = m_CapitalHistory.last()
        val deltaCapital = last - m_StartingCapital
        val deltaCapitalInPercent = (deltaCapital / m_StartingCapital) * 100.0
        val winRate = if (m_TotalSellsMade <= 0) Double.NaN else (m_WinningTrades * 100.0 / m_TotalSellsMade)

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
        val tax = if(m_Taxation == null) "Without" else "With"
        println("Taxes: $tax")
        println("Starting Capital: " + String.format("%.2f", m_StartingCapital) + System.lineSeparator())

        println("Total Capital: " + String.format("%.2f", last))
        println("Delta Capital: " + String.format("%.2f", deltaCapital))
        println("Percent change: " + String.format("%.2f", deltaCapitalInPercent) + "%")
        println()

        println("Total Buys Made: $m_TotalBuysMade")
        println("Total Sells Made: $m_TotalSellsMade")
        println("Winrate: " + String.format("%.2f", winRate) + "%")
        println("Sharpe Ratio: " + String.format("%.2f", utils.Math.sharpeRatio(m_CapitalHistory)))
        println()
        if(debug != DebugMode.None) {
            println("#===============================================================#")
            println("# DEBUG_INFO")

            if(debug is DebugMode.Full || debug is DebugMode.Holding) {
                print("  Holdings: ")
                if (m_Holdings.isEmpty()) println("None")
                else {
                    println()
                    for (item in m_Holdings) println("        $item")
                }
            }

            if(debug is DebugMode.Full || debug is DebugMode.TradeSignal) {
                print("  Buy & Sale trades:")
                if(m_Signlas.isEmpty()) println("None")
                else {
                    println()
                    var counter = 1L
                    for (signal in m_Signlas) {
                        println("        ${(counter++)} ${signal.toReadableText()}")
                    }
                }
            }
        }
    }

    //===========================================================//

    private fun reset() {
        val pair = TradingAlgorithm.create(m_Type, m_SecurityIdentifier, m_From, m_To)

        m_TradingAlgorithm = pair.second
        m_HistoryWeRunAgainst = pair.first

        m_Holdings.clear()
        m_CapitalHistory.clear()
        m_Signlas.clear()

        m_CurrentCapital = m_StartingCapital
        m_TotalBuysMade = 0
        m_TotalSellsMade = 0
        m_WinningTrades = 0
    }

    //===========================================================//

    private fun internalRunBackTest(): Output {
        for (history in m_HistoryWeRunAgainst) {
            val currentPrice = history.closingPrice
            runOneIteration(currentPrice)
        }

        val winRate = if (m_TotalSellsMade <= 0) Double.NaN else (m_WinningTrades.toDouble() / m_TotalSellsMade.toDouble())

        return Output(
            m_StartingCapital,
            m_CapitalHistory.last(),
            m_TotalBuysMade,
            m_TotalSellsMade,
            winRate,
            utils.Math.sharpeRatio(m_CapitalHistory)
        )
    }

    //===========================================================//

    private fun runOneIteration(currentPrice: Double) {
        val ret = m_TradingAlgorithm.run(m_Holdings, m_CurrentCapital, currentPrice)

        var projectedStockCount = getCurrentStockCount()
        if (ret.buy != null) projectedStockCount += ret.buy.amount
        if (ret.sell != null) projectedStockCount -= getSellAmount(ret.sell)

        m_Signlas.add(TradingSignal(
            ret.buy,
            ret.sell,
            currentPrice
        ))

        if (ret.buy != null) {
            m_CurrentCapital -= ret.buy.amount * currentPrice
            m_Holdings.add(SecurityHolding(currentPrice, ret.buy.amount))
            m_TotalBuysMade++
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

    private fun getCurrentStockCount(): Long {
        var count = 0L
        for (holding in m_Holdings) count += holding.amount
        return count
    }

    //===========================================================//
    //===========================================================//
    // Constructor(s)

    constructor(
        type: TradingAlgorithm.Type,
        securityIdentifier: SecurityIdentifier,
        startingCapital: Double,
        taxation: ITaxation? = null,
        from: Instant = Instant.DISTANT_PAST,
        to: Instant = Instant.DISTANT_FUTURE,
    ) {
        require(startingCapital >= 0) { "Capital" }

        m_SecurityIdentifier = securityIdentifier
        m_From = from
        m_To = to

        m_StartingCapital = startingCapital
        m_Taxation = taxation

        m_Type = type
        val pair = TradingAlgorithm.create(m_Type, securityIdentifier, m_From, m_To)
        m_TradingAlgorithm = pair.second
        m_HistoryWeRunAgainst = pair.first

        m_Holdings = ArrayList()
        m_CapitalHistory = ArrayList()
        m_Signlas = ArrayList()

        m_CurrentCapital = m_StartingCapital
        m_TotalBuysMade = 0
        m_TotalSellsMade = 0
        m_WinningTrades = 0
    }

    //===========================================================//
    //===========================================================//
    // Helper Class(es)

    sealed interface DisplayMode {
        data class Display(val debug: DebugMode = DebugMode.None) : DisplayMode
        data object NoDisplay : DisplayMode
    }

    //===========================================================//

    sealed interface DebugMode {
        data object None : DebugMode
        data object Full : DebugMode
        data object Holding : DebugMode
        data object TradeSignal : DebugMode
    }

    //===========================================================//

    data class Output(
        val startingCapital: Double,
        val totalCapital: Double,
        val totalBuysMade: Long,
        val totalSellsMade: Long,
        val tradeWinrate: Double,
        val sharpieRatio: Double
    )
}
