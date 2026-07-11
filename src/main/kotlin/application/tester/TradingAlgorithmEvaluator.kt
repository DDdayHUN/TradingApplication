package application.tester

import data.repository.historical_data.HistoricalMarketDataProvider
import domain.algorithm.TradingAlgorithm
import domain.market.security.SecurityIdentifier
import domain.tax.Taxation
import domain.utils.Math.bottom
import domain.utils.Math.median
import domain.utils.Math.top
import domain.utils.Math.trim
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import format
import java.time.ZoneOffset
import kotlin.math.pow
import kotlin.time.Instant
import kotlin.time.toJavaInstant

//===========================================================//
/**
 * Given an algorithm this class evaluates it
 * and prints the collected data.
 */
//===========================================================//

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
        val listOfSecurityIdentifiers = HistoricalMarketDataProvider.getAllSecurityIdentifiers().getOrThrow()

        val timePeriods = listOf(
            TimePeriod.Year10,
            TimePeriod.Year5,
            TimePeriod.Year2,
            TimePeriod.Year1
        )

        val results = timePeriods.map {
            async {
                years(it, listOfSecurityIdentifiers)
            }
        }.awaitAll().filterNotNull()

        return@coroutineScope Output(results.map { Pair(calculateStatistics(it.first), it.second) })
    }

    //===========================================================//
    //===========================================================//
    // Private Method(es)

    private suspend fun years(cycle: TimePeriod, listOfSecurityIdentifiers: List<SecurityIdentifier>): Pair<List<TradingAlgorithmBackTesterOutputConverted>, TimePeriod>? = coroutineScope {
        val zone = ZoneOffset.UTC
        val windowSizeYears = cycle.toInt()

        val startYear = m_EvaluationStartDate
            .toJavaInstant()
            .atZone(zone)
            .year

        val endYear = m_EvaluationEndDate
            .toJavaInstant()
            .atZone(zone)
            .year


        if ((endYear - windowSizeYears) < startYear) return@coroutineScope null


        val results = (startYear..endYear - windowSizeYears step m_WindowStepYears)
            .map { year ->
                async {
                    val startDate = Instant.parse("${year}-01-01T00:00:00Z")
                    val endDate = Instant.parse("${year + windowSizeYears}-01-01T00:00:00Z")

                    runBackTesters(
                        listOfSecurityIdentifiers,
                        startDate,
                        endDate
                    )
                }
            }
            .awaitAll()
            .flatten()


        if (results.isEmpty()) return@coroutineScope null


        return@coroutineScope Pair(results, cycle)
    }

    //===========================================================//

    private fun calculateStatistics(list: List<TradingAlgorithmBackTesterOutputConverted>): EvaluationStatistics {
        val trim = 0.2

        val capitals = list.map { it.totalCapital }
        val cagrs = list.map { it.cagr }
        val sharpes = list.map { it.sharpeRatio }
        val drawdowns = list.map { it.maxDrawdown }
        val calmar = list.map { it.cagr / it.maxDrawdown }

        return EvaluationStatistics(
            tradingAlgorithmType = m_TradingAlgorithmType,
            taxation = m_TaxationType,
            startingCapital = m_StartingCapital,

            totalCapitalMean = capitals.average(),
            totalCapitalTrimmedMean = capitals.trim(trim).average(),
            totalCapitalMedian = capitals.median(),
            totalCapitalT20 = capitals.top(trim).average(),
            totalCapitalB20 = capitals.bottom(trim).average(),

            cagrMean = cagrs.average(),
            cagrTrimmedMean = cagrs.trim(trim).average(),
            cagrMedian = cagrs.median(),
            cagrT20 = cagrs.top(trim).average(),
            cagrB20 = cagrs.bottom(trim).average(),

            sharpeMean = sharpes.average(),
            sharpeTrimmedMean = sharpes.trim(trim).average(),
            sharpeMedian = sharpes.median(),
            sharpeT20 = sharpes.top(trim).average(),
            sharpeB20 = sharpes.bottom(trim).average(),

            maxDrawdownMean = drawdowns.average(),
            maxDrawdownTrimmedMean = drawdowns.trim(trim).average(),
            maxDrawdownMedian = drawdowns.median(),
            maxDrawdownT20 = drawdowns.bottom(trim).average(),
            maxDrawdownB20 = drawdowns.top(trim).average(),

            calmarMean = calmar.average(),
            calmarTrimmedMean = calmar.trim(trim).average(),
            calmarMedian = calmar.median(),
            calmarT20 = calmar.top(trim).average(),
            calmarB20 = calmar.bottom(trim).average()
        )
    }

    //===========================================================//

    private suspend fun runBackTesters(listOfSecurityIdentifiers: List<SecurityIdentifier>, startDate: Instant, endDate: Instant): List<TradingAlgorithmBackTesterOutputConverted> = coroutineScope {
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

                return@async if (out.tradeWinrate.isNaN() || out.sharpeRatio.isNaN()) null else out
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

    data class Output(val list: List<Pair<EvaluationStatistics, TimePeriod>>) {
        fun display() {

            require(list.isNotEmpty()) { "No evaluation results available." }

            val first = list.first().first
            val tax = if (first.taxation == null) "Without" else "With"

            println("#===============================================================#")
            println("# Algorithm Evaluation | Algorithm: ${first.tradingAlgorithmType}")
            println("#===============================================================#")
            println("Starting Capital: ${first.startingCapital.format(2)}")
            println("Taxes: $tax")
            println()
            println("M = Mean, TM = Trimmed Mean (middle 80%), Md = Median")
            println("Best20 = Average of best 20%, Worst20 = Average of worst 20%")
            println()

            // Total Capital
            header("Total Capital")
            row("M",     { it.totalCapitalMean },        { it.format(2) })
            row("TM",    { it.totalCapitalTrimmedMean }, { it.format(2) })
            row("Md",    { it.totalCapitalMedian },      { it.format(2) })
            row("Best20", { it.totalCapitalT20 },         { it.format(2) })
            row("Worst20", { it.totalCapitalB20 },         { it.format(2) })
            println("-".repeat(66))
            println()

            // CAGR
            header("CAGR")
            row("M",     { it.cagrMean },        { "${(it * 100).format(2)}%" })
            row("TM",    { it.cagrTrimmedMean }, { "${(it * 100).format(2)}%" })
            row("Md",    { it.cagrMedian },      { "${(it * 100).format(2)}%" })
            row("Best20", { it.cagrT20 },         { "${(it * 100).format(2)}%" })
            row("Worst20", { it.cagrB20 },         { "${(it * 100).format(2)}%" })
            println("-".repeat(66))
            println()

            // Max Drawdown
            header("Max Drawdown")
            row("M",     { it.maxDrawdownMean },        { "${(it * 100).format(2)}%" })
            row("TM",    { it.maxDrawdownTrimmedMean }, { "${(it * 100).format(2)}%" })
            row("Md",    { it.maxDrawdownMedian },      { "${(it * 100).format(2)}%" })
            row("Best20", { it.maxDrawdownT20 },         { "${(it * 100).format(2)}%" })
            row("Worst20", { it.maxDrawdownB20 },         { "${(it * 100).format(2)}%" })
            println("-".repeat(66))
            println()

            // Sharpe
            header("Sharpe")
            row("M",     { it.sharpeMean },        { it.format(2) })
            row("TM",    { it.sharpeTrimmedMean }, { it.format(2) })
            row("Md",    { it.sharpeMedian },      { it.format(2) })
            row("Best20", { it.sharpeT20 },         { it.format(2) })
            row("Worst20", { it.sharpeB20 },         { it.format(2) })
            println("-".repeat(66))
            println()

            // Calmar
            header("Calmar")
            row("M",     { it.calmarMean },        { it.format(2) })
            row("TM",    { it.calmarTrimmedMean }, { it.format(2) })
            row("Md",    { it.calmarMedian },      { it.format(2) })
            row("Best20", { it.calmarT20 },         { it.format(2) })
            row("Worst20", { it.calmarB20 },         { it.format(2) })
            println("-".repeat(66))
            println()
        }

        //===========================================================//

        private fun header(title: String) {
            println(title)

            val periods = TimePeriod.entries.joinToString(" ") {
                "| ${it.toString().padStart(10)}"
            }

            println("| ${"Statistic".padEnd(10)} $periods |")
            println("-".repeat(13 + TimePeriod.entries.size * 13))
        }

        //===========================================================//

        private fun row(
            label: String,
            value: (EvaluationStatistics) -> Double,
            formatter: (Double) -> String
        ) {
            val byPeriod = list.associate { it.second to it.first }

            val values = TimePeriod.entries.joinToString(" ") { period ->
                "| ${
                    byPeriod[period]
                        ?.let { formatter(value(it)).padStart(10) }
                        ?: " ".repeat(10)
                }"
            }

            println("| ${label.padEnd(10)} $values |")
        }
    }

    //===========================================================//

    data class EvaluationStatistics(
        val tradingAlgorithmType: TradingAlgorithm.Type,
        val taxation: Taxation.Type?,
        val startingCapital: Double,

        val totalCapitalMean: Double,
        val totalCapitalTrimmedMean: Double,
        val totalCapitalMedian: Double,
        val totalCapitalT20: Double,
        val totalCapitalB20: Double,

        val cagrMean: Double,
        val cagrTrimmedMean: Double,
        val cagrMedian: Double,
        val cagrT20: Double,
        val cagrB20: Double,

        val sharpeMean: Double,
        val sharpeTrimmedMean: Double,
        val sharpeMedian: Double,
        val sharpeT20: Double,
        val sharpeB20: Double,

        val maxDrawdownMean: Double,
        val maxDrawdownTrimmedMean: Double,
        val maxDrawdownMedian: Double,
        val maxDrawdownT20: Double,
        val maxDrawdownB20: Double,

        val calmarMean: Double,
        val calmarTrimmedMean: Double,
        val calmarMedian: Double,
        val calmarT20: Double,
        val calmarB20: Double
    )

    //===========================================================//

    data class TradingAlgorithmBackTesterOutputConverted(
        val tradingAlgorithmType: TradingAlgorithm.Type,
        val taxation: Taxation.Type?,
        val startingCapital: Double,
        val totalCapital: Double,
        val totalBuysMade: Double,
        val totalSellsMade: Double,
        val forceClosedTrades: Double,
        val maxDrawdown: Double,
        val sharpeRatio: Double,
        val cagr: Double
    )

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

        companion object {
            val entries = listOf(
                Year10,
                Year5,
                Year2,
                Year1
            )
        }

        fun toInt(): Int
    }

    //===========================================================//
    //===========================================================//
    // Extension(s)

    private fun TradingAlgorithmBackTester.Output.toAverageOutput(): TradingAlgorithmBackTesterOutputConverted {
        val years = java.time.Duration.between(from.toJavaInstant(), to.toJavaInstant()).toDays().toDouble() / 365.2425
        val cagr = ((totalCapital / startingCapital).pow(1.0 / years) - 1.0)

        return TradingAlgorithmBackTesterOutputConverted(
            tradingAlgorithmType,
            taxation,
            startingCapital,
            totalCapital,
            totalBuysMade.toDouble(),
            totalSellsMade.toDouble(),
            forceClosedTrades.toDouble(),
            maxDrawdown,
            sharpeRatio,
            cagr
        )
    }
}