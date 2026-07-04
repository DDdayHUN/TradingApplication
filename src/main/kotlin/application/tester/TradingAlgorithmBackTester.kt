package application.tester

import domain.algorithm.ITradingAlgorithm
import domain.algorithm.TradingAlgorithm
import domain.signal.TradingSignal
import domain.assets.security.SecurityHistory
import domain.assets.security.SecurityHolding
import domain.assets.security.SecurityIdentifier
import domain.tax.ITaxation
import utils.format
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
    private var m_ForceClosedTrades: Long

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
        println("# Algorithm Backtesting")
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
        println("Starting Capital: ${m_StartingCapital.format(2)}")
        println()

        println("Total Capital: ${last.format(2)}")
        println("Delta Capital: ${deltaCapital.format(2)}")
        println("Percent Change: ${deltaCapitalInPercent.format(2)}%")
        println()

        println("Total Buys Made: $m_TotalBuysMade")
        println("Total Sells Made: $m_TotalSellsMade")
        println("Force Closed Trades: $m_ForceClosedTrades")
        println("Winrate: ${winRate.format(2)}%")
        println("Sharpe Ratio: ${utils.Math.sharpeRatio(m_CapitalHistory).format(2)}")
        println()

        if(debug != DebugMode.None) {
            println("#===============================================================#")
            println("# DEBUG_INFO")
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
        m_ForceClosedTrades = 0
    }

    //===========================================================//

    private fun internalRunBackTest(): Output {
        for (history in m_HistoryWeRunAgainst) {
            val currentPrice = history.closingPrice
            runOneIteration(currentPrice)
        }

        forceSell()

        val winRate = if (m_TotalSellsMade <= 0) Double.NaN else (m_WinningTrades.toDouble() / m_TotalSellsMade.toDouble())

        return Output(
            m_StartingCapital,
            m_CapitalHistory.last(),
            m_TotalBuysMade,
            m_TotalSellsMade,
            m_ForceClosedTrades,
            winRate,
            utils.Math.sharpeRatio(m_CapitalHistory)
        )
    }

    //===========================================================//

    private fun runOneIteration(currentPrice: Double) {
        val ret = m_TradingAlgorithm.run(m_Holdings, m_CurrentCapital, currentPrice)

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

    private fun forceSell() {
        val lastPrice = m_HistoryWeRunAgainst.last().closingPrice
        for(holding in m_Holdings) {
            if (m_Taxation == null) m_CurrentCapital += holding.amount * lastPrice
            else {
                val revenue = holding.amount * lastPrice
                val costBasis = holding.amount * holding.entryPrice
                m_CurrentCapital += m_Taxation.calculateRevenueAfterTax(revenue, costBasis)
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
        m_ForceClosedTrades = 0
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
        data object TradeSignal : DebugMode
    }

    //===========================================================//

    data class Output(
        val startingCapital: Double,
        val totalCapital: Double,
        val totalBuysMade: Long,
        val totalSellsMade: Long,
        val forceClosedTrades: Long,
        val tradeWinrate: Double,
        val sharpieRatio: Double
    )
}
