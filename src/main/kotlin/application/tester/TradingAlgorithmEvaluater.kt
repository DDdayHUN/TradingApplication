package application.tester

import data.HistoricalMarketDataProvider
import domain.algorithm.TradingAlgorithm
import domain.assets.security.SecurityIdentifier
import domain.tax.ITaxation
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.runBlocking
import kotlin.time.Instant

class TradingAlgorithmEvaluater {
    //===========================================================//
    //===========================================================//
    // Private Field(s)

    private val m_TradingAlgorithmType: TradingAlgorithm.Type
    private val m_Taxation: ITaxation?
    private val m_Capital: Double

    //===========================================================//
    //===========================================================//
    // Public Method(es)

    suspend fun runEvaluation() {
        val listOfSecurityIdentifiers = HistoricalMarketDataProvider.getAllSecurityIdentifiers()

        println("#===============================================================#")
        println("# Algorithm Evaluation")
        println("#===============================================================#")
        val tax = if(m_Taxation == null) "Without" else "With"
        println("Taxes: $tax")
        coroutineScope {
            val a1 = async { allTime(listOfSecurityIdentifiers) }
            val a2 = async { years(5L,listOfSecurityIdentifiers) }
            val a3 = async { years(1L, listOfSecurityIdentifiers) }

            display(a1.await(), TimePeriod.AllTime)
            display(a2.await(), TimePeriod.Year5)
            display(a3.await(), TimePeriod.Year1)
        }
    }

    //===========================================================//
    //===========================================================//
    // Private Method(es)

    private suspend fun allTime(listOfSecurityIdentifiers: List<SecurityIdentifier>): TradingAlgorithmBackTester.Output {
        return averager(init(listOfSecurityIdentifiers))
    }

    //===========================================================//

    private suspend fun years(increment: Long, listOfSecurityIdentifiers: List<SecurityIdentifier>): TradingAlgorithmBackTester.Output {
        val outputs: MutableList<TradingAlgorithmBackTester.Output> = ArrayList()

        for (currentYear in 2015L until 2025L step increment) {
            val startDate = Instant.parse("${currentYear}-01-01T00:00:00Z")
            val endDate = Instant.parse("${currentYear + increment}-01-01T00:00:00Z")
            outputs.addAll(init(listOfSecurityIdentifiers, startDate, endDate))
        }

        return averager(outputs)
    }

    //===========================================================//

    private fun averager(listOfOutputs: List<TradingAlgorithmBackTester.Output>): TradingAlgorithmBackTester.Output {
        var totalCapitalSum = 0.0
        var totalBuysMade = 0L
        var totalSellsMade = 0L
        var totalWins = 0.0
        var totalTrades = 0L

        // NOTE: For a real Sharpe ratio, consider logging portfolio-wide daily returns.
        // This average is still a naive approach, but kept for simplicity here. (Yes GPT shet)
        var sharpieRatioSum = 0.0

        for (output in listOfOutputs) {
            totalCapitalSum += output.totalCapital
            totalBuysMade += output.totalBuysMade
            totalSellsMade += output.totalSellsMade

            val stockTotalTrades = output.totalBuysMade + output.totalSellsMade
            totalTrades += stockTotalTrades
            totalWins += (output.tradeWinrate * stockTotalTrades)

            sharpieRatioSum += output.sharpieRatio
        }

        // Calculate true global winrate
        val globalWinrate = if (totalTrades > 0) totalWins / totalTrades else 0.0

        return TradingAlgorithmBackTester.Output(
            startingCapital = m_Capital,
            totalCapital = totalCapitalSum / listOfOutputs.size, // Average ending capital
            totalBuysMade = totalBuysMade / listOfOutputs.size,
            totalSellsMade = totalSellsMade / listOfOutputs.size,
            tradeWinrate = globalWinrate,
            sharpieRatio = sharpieRatioSum / listOfOutputs.size
        )
    }

    //===========================================================//

    private fun display(average: TradingAlgorithmBackTester.Output, timePeriod: TimePeriod) {
        println("# Time Period: $timePeriod")
        println("Starting Capital: " + String.format("%.2f", m_Capital))
        println()

        val deltaCapital = average.totalCapital - m_Capital
        println("Average Total Capital: " + String.format("%.2f", average.totalCapital))
        println("Average Delta Capital: " + String.format("%.2f", deltaCapital))
        println("Average Percent Change: " + String.format("%.2f", (deltaCapital / m_Capital) * 100.0) + "%")
        println()

        println("Average Total Buys Made: ${average.totalBuysMade}")
        println("Average Total Sells Made: ${average.totalSellsMade}")
        println("Average Winrate: " + String.format("%.2f", average.tradeWinrate * 100.0) + "%")
        println("Average Sharpe Ratio: " + String.format("%.2f", average.sharpieRatio))
        println()
    }

    //===========================================================//

    private suspend fun init(listOfSecurityIdentifiers: List<SecurityIdentifier>, startDate: Instant = Instant.DISTANT_PAST, endDate: Instant = Instant.DISTANT_FUTURE): List<TradingAlgorithmBackTester.Output> = coroutineScope {
        val outputs: MutableList<TradingAlgorithmBackTester.Output> = ArrayList()
        val deferredOutputs = listOfSecurityIdentifiers
            .filter { it.isin != "US84615Q1031" } // TODO: SpaceX initialization fix
            .map { securityIdentifier ->
                async {
                    TradingAlgorithmBackTester(
                        type = m_TradingAlgorithmType,
                        securityIdentifier = securityIdentifier,
                        startingCapital = m_Capital,
                        taxation = m_Taxation,
                        from = startDate,
                        to = endDate
                    ).runBackTest()
                }
            }

        outputs.addAll(deferredOutputs.awaitAll())
        return@coroutineScope outputs
    }

    //===========================================================//
    //===========================================================//
    // Constructor(s)

    constructor(tradingAlgorithmType: TradingAlgorithm.Type, taxation: ITaxation?, capital: Double) {
        m_Capital = capital
        m_Taxation = taxation
        m_TradingAlgorithmType = tradingAlgorithmType
    }

    //===========================================================//
    //===========================================================//
    // Helper Class(es)

    private sealed interface TimePeriod {
        data object AllTime : TimePeriod {
            override fun toString(): String = "AllTime"
        }
        data object Year5 : TimePeriod {
            override fun toString(): String = "5 Year"
        }
        data object Year1 : TimePeriod {
            override fun toString(): String = "1 Year"
        }
    }
}