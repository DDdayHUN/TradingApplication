package application.tester

import data.repository.HistoricalMarketDataProvider
import domain.algorithm.TradingAlgorithm
import domain.assets.security.SecurityIdentifier
import domain.tax.Taxation
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import utils.format
import kotlin.math.pow
import kotlin.time.Instant

class TradingAlgorithmEvaluater {
    //===========================================================//
    //===========================================================//
    // Private Field(s)

    private val m_TradingAlgorithmType: TradingAlgorithm.Type
    private val m_TaxationType: Taxation.Type?
    private val m_Capital: Double

    private val m_BackTestFilter: (SecurityIdentifier) -> Boolean

    //===========================================================//
    //===========================================================//
    // Public Method(es)

    suspend fun runEvaluation() = coroutineScope {
        val a0 = async { HistoricalMarketDataProvider.getAllSecurityIdentifiers() }

        val listOfSecurityIdentifiers = a0.await()
        val a1 = async { years(10, listOfSecurityIdentifiers) }
        val a2 = async { years(5,listOfSecurityIdentifiers) }
        val a3 = async { years(2,listOfSecurityIdentifiers) }
        val a4 = async { years(1, listOfSecurityIdentifiers) }

        val r1 = a1.await()
        val r2 = a2.await()
        val r3 = a3.await()
        val r4 = a4.await()

        println("#===============================================================#")
        println("# Algorithm Evaluation | Algorithm: $m_TradingAlgorithmType")
        println("#===============================================================#")
        println("Starting Capital: ${m_Capital.format(2)}")
        val tax = if(m_TaxationType == null) "Without" else "With"
        println("Taxes: $tax")
        println("(All subsequent values are averages)")
        println()
        display(r1, TimePeriod.Year10)
        display(r2, TimePeriod.Year5)
        display(r3, TimePeriod.Year2)
        display(r4, TimePeriod.Year1)
    }

    //===========================================================//
    //===========================================================//
    // Private Method(es)

    private suspend fun years(increment: Int, listOfSecurityIdentifiers: List<SecurityIdentifier>): AverageOutput = coroutineScope {
        val results = (2015 until 2025 step increment).map { currentYear ->
            async {
                val startDate = Instant.parse("${currentYear}-01-01T00:00:00Z")
                val endDate = Instant.parse("${currentYear + increment}-01-01T00:00:00Z")
                averager(runBackTesters(listOfSecurityIdentifiers, startDate, endDate))
            }
        }
        return@coroutineScope averager(results.awaitAll())
    }

    //===========================================================//

    private fun averager(listOfOutputs: List<AverageOutput>): AverageOutput {
        var totalCapitalSum = 0.0
        var totalBuysMade = 0.0
        var totalSellsMade = 0.0
        var forceClosedTrades = 0.0
        var totalWins = 0.0

        // NOTE: For a real Sharpe ratio, consider logging portfolio-wide daily returns.
        // This average is still a naive approach, but kept for simplicity here. (Yes GPT shet)
        var sharpieRatioSum = 0.0

        for (output in listOfOutputs) {
            totalCapitalSum += output.totalCapital - m_Capital
            totalBuysMade += output.totalBuysMade
            totalSellsMade += output.totalSellsMade
            forceClosedTrades += output.forceClosedTrades
            totalWins += output.tradeWinrate
            sharpieRatioSum += output.sharpieRatio
        }

        return AverageOutput(
            startingCapital = m_Capital,
            totalCapital = totalCapitalSum / listOfOutputs.size + m_Capital,
            totalBuysMade =totalBuysMade / listOfOutputs.size,
            totalSellsMade = totalSellsMade / listOfOutputs.size,
            forceClosedTrades = forceClosedTrades / listOfOutputs.size,
            tradeWinrate = totalWins / listOfOutputs.size,
            sharpieRatio = sharpieRatioSum / listOfOutputs.size
        )
    }

    //===========================================================//

    private fun display(average: AverageOutput, timePeriod: TimePeriod) {
        println("# Time Period: $timePeriod")
        println()

        val deltaCapital = average.totalCapital - m_Capital
        val deltaCapitalInPercent = (deltaCapital / m_Capital) * 100.0
        val yearlyPercentChange = ((average.totalCapital / m_Capital).pow(1.0 / timePeriod.toDouble()) - 1.0) * 100.0
        println("Total Capital: ${average.totalCapital.format(2)}")
        println("Delta Capital: ${deltaCapital.format(2)}")
        println("Percent Change: ${deltaCapitalInPercent.format(2)}%")
        println("Yearly Percent Change: ${yearlyPercentChange.format(2)}%")
        println()

        println("Total Buys Made: ${average.totalBuysMade.format(2)}")
        println("Total Sells Made: ${average.totalSellsMade.format(2)}")
        println("Force Closed Trades: ${average.forceClosedTrades.format(2)}")
        println("Winrate: ${(average.tradeWinrate * 100.0).format(2)}%")
        println("Sharpe Ratio: ${average.sharpieRatio.format(2)}")
        println()
    }

    //===========================================================//

    private suspend fun runBackTesters(listOfSecurityIdentifiers: List<SecurityIdentifier>, startDate: Instant = Instant.DISTANT_PAST, endDate: Instant = Instant.DISTANT_FUTURE): List<AverageOutput> = coroutineScope {
        val outputs: MutableList<TradingAlgorithmBackTester.Output> = ArrayList()
        val deferredOutputs = listOfSecurityIdentifiers
            .filter(m_BackTestFilter)
            .map { securityIdentifier ->
                async {
                    val taxation = if(m_TaxationType == null) null else Taxation.create(m_TaxationType)
                    val out = TradingAlgorithmBackTester(
                        type = m_TradingAlgorithmType,
                        securityIdentifier = securityIdentifier,
                        startingCapital = m_Capital,
                        taxation = taxation,
                        from = startDate,
                        to = endDate
                    ).runBackTest()

                    return@async if (out.tradeWinrate.isNaN() || out.sharpieRatio.isNaN()) null else out
                }
            }

        outputs.addAll(deferredOutputs.awaitAll().filterNotNull())
        return@coroutineScope outputs.map { it.toAverageOutput() }
    }

    //===========================================================//
    //===========================================================//
    // Constructor(s)

    constructor(tradingAlgorithmType: TradingAlgorithm.Type, capital: Double, taxationType: Taxation.Type? = null) {
        m_TradingAlgorithmType = tradingAlgorithmType
        m_Capital = capital
        m_TaxationType = taxationType

        m_BackTestFilter = { it.isin != "US84615Q1031" && it.isin != "US67066G1040" } // TODO : Something is sus about the SpaceX stock, so we just ignore it.
    }

    //===========================================================//
    //===========================================================//
    // Helper Class(es)

    private sealed interface TimePeriod {
        data object Year10 : TimePeriod {
            override fun toString(): String = "10 Year"
            override fun toDouble(): Double = 10.0
        }
        data object Year5 : TimePeriod {
            override fun toString(): String = "5 Year"
            override fun toDouble(): Double = 5.0
        }
        data object Year2 : TimePeriod {
            override fun toString(): String = "2 Year"
            override fun toDouble(): Double = 2.0
        }
        data object Year1 : TimePeriod {
            override fun toString(): String = "1 Year"
            override fun toDouble(): Double = 1.0
        }

        fun toDouble(): Double
    }

    //===========================================================//

    private data class AverageOutput(
        val startingCapital: Double,
        val totalCapital: Double,
        val totalBuysMade: Double,
        val totalSellsMade: Double,
        val forceClosedTrades: Double,
        val tradeWinrate: Double,
        val sharpieRatio: Double
    )

    //===========================================================//
    //===========================================================//
    // Extension(s)

    private fun TradingAlgorithmBackTester.Output.toAverageOutput(): AverageOutput {
        return AverageOutput(
            startingCapital,
            totalCapital,
            totalBuysMade.toDouble(),
            totalSellsMade.toDouble(),
            forceClosedTrades.toDouble(),
            tradeWinrate,
            sharpieRatio
        )
    }
}