package application.tester

import data.HistoricalMarketDataProvider
import domain.algorithm.TradingAlgorithm
import domain.tax.ITaxation
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlin.time.Instant

class TradingAlgorithmEvaluater {
    //===========================================================//
    //===========================================================//
    // Private Field(s)

    private val m_TradingAlgorithmType: TradingAlgorithm.Type
    private val m_Taxation: ITaxation?
    private val m_Capital: Double

    private val m_StartDate: Instant
    private val m_EndDate: Instant

    private val m_Outputs: MutableList<TradingAlgorithmBackTester.Output>

    private lateinit var m_Average: TradingAlgorithmBackTester.Output

    //===========================================================//
    //===========================================================//
    // Public Method(es)

    suspend fun runEvaluation() = coroutineScope {
        m_Outputs.clear()

        val listOfSecurityIdentifiers = HistoricalMarketDataProvider.getAllSecurityIdentifiers()

        // 1. Run all backtests in PARALLEL using async/awaitAll
        val deferredOutputs = listOfSecurityIdentifiers
            .filter { it.isin != "US84615Q1031" } // TODO: SpaceX initialization fix
            .map { securityIdentifier ->
                async {
                    TradingAlgorithmBackTester(
                        type = m_TradingAlgorithmType,
                        securityIdentifier = securityIdentifier,
                        startingCapital = m_Capital,
                        taxation = m_Taxation,
                        from = m_StartDate,
                        to = m_EndDate
                    ).runBackTest()
                }
            }

        m_Outputs.addAll(deferredOutputs.awaitAll())

        if (m_Outputs.isEmpty()) return@coroutineScope

        var totalCapitalSum = 0.0
        var totalBuysMade = 0L
        var totalSellsMade = 0L
        var totalWins = 0.0
        var totalTrades = 0L

        // NOTE: For a real Sharpe ratio, consider logging portfolio-wide daily returns.
        // This average is still a naive approach, but kept for simplicity here. (Yes GPT shet)
        var sharpieRatioSum = 0.0

        for (output in m_Outputs) {
            totalCapitalSum += output.totalCapital
            totalBuysMade += output.totalBuysMade
            totalSellsMade += output.totalSellsMade

            val stockTotalTrades = output.totalBuysMade + output.totalSellsMade // or however you track trades
            totalTrades += stockTotalTrades
            totalWins += (output.tradeWinrate * stockTotalTrades)

            sharpieRatioSum += output.sharpieRatio
        }

        val sampleSize = m_Outputs.size.toDouble()

        // Calculate true global winrate
        val globalWinrate = if (totalTrades > 0) totalWins / totalTrades else 0.0

        m_Average = TradingAlgorithmBackTester.Output(
            startingCapital = m_Capital,
            totalCapital = totalCapitalSum / sampleSize, // Average ending capital
            totalBuysMade = totalBuysMade / m_Outputs.size,
            totalSellsMade = totalSellsMade / m_Outputs.size,
            tradeWinrate = globalWinrate,
            sharpieRatio = sharpieRatioSum / sampleSize
        )

        display()
    }

    //===========================================================//
    //===========================================================//
    // Private Method(es)

    private fun display() {
        println("#===============================================================#")
        println("# Algorithm Evaluation")
        println("#===============================================================#")

        val tax = if(m_Taxation == null) "Without" else "With"
        println("Taxes: $tax")
        println("Starting Capital: " + String.format("%.2f", m_Capital))
        println()

        val deltaCapital = m_Average.totalCapital - m_Capital
        println("Average Total Capital: " + String.format("%.2f", m_Average.totalCapital))
        println("Average Delta Capital: " + String.format("%.2f", deltaCapital))
        println("Average Percent Change: " + String.format("%.2f", (deltaCapital / m_Capital) * 100.0) + "%")
        println()

        println("Average Total Buys Made: ${m_Average.totalBuysMade}")
        println("Average Total Sells Made: ${m_Average.totalSellsMade}")
        println("Average Winrate: " + String.format("%.2f", m_Average.tradeWinrate * 100.0) + "%")
        println("Average Sharpe Ratio: " + String.format("%.2f", m_Average.sharpieRatio))
        println()
    }

    //===========================================================//
    //===========================================================//
    // Constructor(s)

    constructor(tradingAlgorithmType: TradingAlgorithm.Type, taxation: ITaxation?, capital: Double, startDate: Instant, endDate: Instant) {
        m_Capital = capital
        m_Taxation = taxation
        m_TradingAlgorithmType = tradingAlgorithmType

        m_StartDate = startDate
        m_EndDate = endDate

        m_Outputs = ArrayList()
    }
}