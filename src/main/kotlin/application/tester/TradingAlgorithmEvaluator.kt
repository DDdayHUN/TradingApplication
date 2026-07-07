package application.tester

import data.repository.historical_data.HistoricalMarketDataProvider
import domain.algorithm.TradingAlgorithm
import domain.market.security.SecurityIdentifier
import domain.tax.Taxation
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import format
import java.time.ZoneId
import kotlin.math.pow
import kotlin.time.Instant
import kotlin.time.toJavaInstant

class TradingAlgorithmEvaluator {
    //===========================================================//
    //===========================================================//
    // Private Field(s)

    private val m_TradingAlgorithmType: TradingAlgorithm.Type
    private val m_TaxationType: Taxation.Type?
    private val m_StartingCapital: Double

    private val m_EvaluationStartDate: Instant
    private val m_EvaluationEndDate: Instant
    private val m_WindowStepYears: Int

    //===========================================================//
    //===========================================================//
    // Public Method(es)

    suspend fun runEvaluation(): Output = coroutineScope {
        val listOfSecurityIdentifiers = HistoricalMarketDataProvider.getAllSecurityIdentifiers()

        val timePeriods = listOf(
            TimePeriod.Year10,
            TimePeriod.Year5,
            TimePeriod.Year2,
            TimePeriod.Year1
        )

        val results = timePeriods.map { timePeriod ->
            async{
                years(timePeriod, listOfSecurityIdentifiers)
            }
        }.awaitAll().filterNotNull()

        return@coroutineScope Output(results)
    }

    //===========================================================//
    //===========================================================//
    // Private Method(es)

    private suspend fun years(cycle: TimePeriod, listOfSecurityIdentifiers: List<SecurityIdentifier>): Pair<AverageOutput, TimePeriod>? = coroutineScope {
        val windowSizeYears = cycle.toInt()

        val startYear = m_EvaluationStartDate.toJavaInstant().atZone(ZoneId.systemDefault()).year
        val endYear = m_EvaluationEndDate.toJavaInstant().atZone(ZoneId.systemDefault()).year

        if((endYear-windowSizeYears) < startYear) return@coroutineScope null

        val result = (startYear..endYear - windowSizeYears step m_WindowStepYears)
            .map {year ->
                async {
                    val startDate = Instant.parse("${year}-01-01T00:00:00Z")
                    val endDate = Instant.parse("${year + windowSizeYears}-01-01T00:00:00Z")

                    averager(
                        runBackTesters(
                            listOfSecurityIdentifiers,
                            startDate,
                            endDate
                        )
                    )
                }
            }

        if(result.isEmpty()) return@coroutineScope null

        return@coroutineScope Pair(
            averager(result.awaitAll()),
            cycle
        )
    }

    //===========================================================//

    private fun averager(list: List<AverageOutput>): AverageOutput {
        val avgTotalCapital = list.map { it.totalCapital - it.startingCapital }.average() + m_StartingCapital
        val avgBuysMade = list.map { it.totalBuysMade }.average()
        val avgSellsMade = list.map { it.totalSellsMade }.average()
        val avgForceClosedTrades = list.map { it.forceClosedTrades }.average()
        val avgTotalWins = list.map { it.tradeWinrate }.average()
        val avgSharpieRatio = list.map { it.sharpieRatio }.average()
        val avgYearlyPercentChangeOfCapital = list.map { it.yearlyPercentChangeOfCapital }.average()

        return AverageOutput(
            m_TradingAlgorithmType,
            m_TaxationType,
            m_StartingCapital,
            avgTotalCapital,
            avgBuysMade,
            avgSellsMade,
            avgForceClosedTrades,
            avgTotalWins,
            avgSharpieRatio,
            avgYearlyPercentChangeOfCapital
        )
    }

    //===========================================================//

    private suspend fun runBackTesters(listOfSecurityIdentifiers: List<SecurityIdentifier>, startDate: Instant, endDate: Instant): List<AverageOutput> = coroutineScope {
        val outputs = listOfSecurityIdentifiers.map { securityIdentifier ->
            async {
                val out = TradingAlgorithmBackTester(
                    type = m_TradingAlgorithmType,
                    securityIdentifier = securityIdentifier,
                    startingCapital = m_StartingCapital,
                    taxation = m_TaxationType,
                    from = startDate,
                    to = endDate
                ).runBackTest()

                return@async if (out.tradeWinrate.isNaN() || out.sharpieRatio.isNaN()) null else out
            }
        }.awaitAll()

        return@coroutineScope outputs.filterNotNull().map { it.toAverageOutput() }
    }

    //===========================================================//
    //===========================================================//
    // Constructor(s)

    constructor(
        tradingAlgorithmType: TradingAlgorithm.Type,
        capital: Double,
        taxation: Taxation.Type? = null,
        evaluationStartYear: Instant = Instant.parse("2015-01-01T00:00:00Z"),
        evaluationEndYear: Instant = Instant.parse("2025-01-01T00:00:00Z"),
        windowStepYears: Int = 1

    ) {
        m_TradingAlgorithmType = tradingAlgorithmType
        m_StartingCapital = capital
        m_TaxationType = taxation
        m_EvaluationStartDate = evaluationStartYear
        m_EvaluationEndDate = evaluationEndYear
        m_WindowStepYears = windowStepYears
    }

    //===========================================================//
    //===========================================================//
    // Helper Class(es)

    data class Output(val list: List<Pair<AverageOutput, TimePeriod>>) {
        fun display() {

            if(list.isEmpty()){
                println("No evaluation results available.")
                return
            }

            val firstElement = list.first().first
            val tax = if(firstElement.taxation == null) "Without" else "With"
            println("#===============================================================#")
            println("# Algorithm Evaluation | Algorithm: ${firstElement.tradingAlgorithmType}")
            println("#===============================================================#")
            println("Starting Capital: ${firstElement.startingCapital.format(2)}")
            println("Taxes: $tax")
            println()
            // NOTE: YEAH...that was not me xd, wtf is this gpt
            println(
                "| ${"Period".padEnd(8)} " +
                        "| ${"Final Capital".padStart(14)} " +
                        "| ${"Profit".padStart(12)} " +
                        "| ${"Profit %".padStart(10)} " +
                        "| ${"Yearly %".padStart(10)} " +
                        "| ${"Buys".padStart(8)} " +
                        "| ${"Sells".padStart(8)} " +
                        "| ${"Forced".padStart(8)} " +
                        "| ${"Winrate".padStart(9)} " +
                        "| ${"Sharpe".padStart(8)} |"
            )

            println("-".repeat(126))

            list.forEach { result ->
                val average = result.first
                val period = result.second

                val profit = average.totalCapital - average.startingCapital
                val profitPercent = (profit / average.startingCapital) * 100.0

                println(
                    "| ${period.toString().padEnd(8)} " +
                            "| ${average.totalCapital.format(2).padStart(14)} " +
                            "| ${profit.format(2).padStart(12)} " +
                            "| ${profitPercent.format(2).padStart(9)}% " +
                            "| ${average.yearlyPercentChangeOfCapital.format(2).padStart(9)}% " +
                            "| ${average.totalBuysMade.format(2).padStart(8)} " +
                            "| ${average.totalSellsMade.format(2).padStart(8)} " +
                            "| ${average.forceClosedTrades.format(2).padStart(8)} " +
                            "| ${formatWinrate(average.tradeWinrate).padStart(9)} " +
                            "| ${average.sharpieRatio.format(2).padStart(8)} |"
                )
            }

            println()
        }

        //===========================================================//
        //===========================================================//
        // Helper Methods(es)

        private fun formatWinrate(value: Double): String {
            return if (value <= 1.0) {
                "${(value * 100.0).format(2)}%"
            } else {
                "${value.format(2)}%"
            }
        }
    }

    //===========================================================//

    @Suppress("DuplicatedCode")
    data class AverageOutput(
        val tradingAlgorithmType: TradingAlgorithm.Type,
        val taxation: Taxation.Type?,
        val startingCapital: Double,
        val totalCapital: Double,
        val totalBuysMade: Double,
        val totalSellsMade: Double,
        val forceClosedTrades: Double,
        val tradeWinrate: Double,
        val sharpieRatio: Double,
        val yearlyPercentChangeOfCapital: Double
    ) {
//        fun display() {
//            val deltaCapital = totalCapital - startingCapital
//            val deltaCapitalInPercent = (deltaCapital / startingCapital) * 100.0
//
//            println("Total Capital: ${totalCapital.format(2)}")
//            println("Delta Capital: ${deltaCapital.format(2)}")
//            println("Percent Change: ${deltaCapitalInPercent.format(2)}%")
//            println("Yearly Percent Change: ${yearlyPercentChangeOfCapital.format(2)}%")
//            println()
//
//            println("Total Buys Made: ${totalBuysMade.format(2)}")
//            println("Total Sells Made: ${totalSellsMade.format(2)}")
//            println("Force Closed Trades: ${forceClosedTrades.format(2)}")
//            println("Winrate: ${tradeWinrate.format(2)}%")
//            println("Sharpe Ratio: ${sharpieRatio.format(2)}")
//            println()
//        }
    }

    //===========================================================//

    sealed interface TimePeriod {
        data object Year10 : TimePeriod {
            override fun toString(): String = "10 Year"
            override fun toInt(): Int = 10
        }
        data object Year5 : TimePeriod {
            override fun toString(): String = "5 Year"
            override fun toInt(): Int = 5
        }
        data object Year2 : TimePeriod {
            override fun toString(): String = "2 Year"
            override fun toInt(): Int = 2
        }
        data object Year1 : TimePeriod {
            override fun toString(): String = "1 Year"
            override fun toInt(): Int = 1
        }

        fun toInt(): Int
    }

    //===========================================================//
    //===========================================================//
    // Extension(s)

    private fun TradingAlgorithmBackTester.Output.toAverageOutput(): AverageOutput {
        val years = java.time.Duration.between(from.toJavaInstant(), to.toJavaInstant()).toDays().toDouble() / 365.2425
        val yearlyPercentChange = ((totalCapital / startingCapital).pow(1.0 / years) - 1.0) * 100.0

        return AverageOutput(
            tradingAlgorithmType,
            taxation,
            startingCapital,
            totalCapital,
            totalBuysMade.toDouble(),
            totalSellsMade.toDouble(),
            forceClosedTrades.toDouble(),
            tradeWinrate,
            sharpieRatio,
            yearlyPercentChange
        )
    }
}