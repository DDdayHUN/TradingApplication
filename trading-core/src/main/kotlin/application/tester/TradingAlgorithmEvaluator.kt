package application.tester

import data.repository.historical_data.IHistoricalMarketDataProvider
import domain.algorithm.TradingAlgorithm
import domain.market.security.SecurityHistory
import domain.market.security.SecurityIdentifier
import domain.tax.Taxation
import domain.trader.TradingOrder
import domain.utils.Math.bottom
import domain.utils.Math.median
import domain.utils.Math.top
import domain.utils.Math.trim
import format
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import java.time.Duration
import java.time.ZoneOffset
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.flatten
import kotlin.collections.mapValues
import kotlin.math.abs
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

    private val m_Provider: IHistoricalMarketDataProvider
    private val m_TradingAlgorithmType: TradingAlgorithm.Type
    private val m_TaxationType: Taxation.Type?
    private val m_StartingCapital: Double

    private val m_EvaluationStartDate: Instant
    private val m_EvaluationEndDate: Instant
    private val m_WindowStepYears: Int

    //===========================================================//
    //===========================================================//
    // Public Method(es)

    suspend fun runEvaluationOnAll(): Output {
        return runEvaluation(m_Provider.getAllSecurityIdentifiers().getOrThrow())
    }

    //===========================================================//

    suspend fun runEvaluation(securityIdentifiers: List<SecurityIdentifier>): Output = coroutineScope {
        val timePeriods = listOf(
            TimePeriod.Year10,
            TimePeriod.Year5,
            TimePeriod.Year3,
            TimePeriod.Year2,
            TimePeriod.Year1
        )

        val results = timePeriods
            .mapNotNull { years(it, securityIdentifiers) }

        return@coroutineScope Output(results)
    }

    //===========================================================//
    //===========================================================//
    // Private Method(es)

    // TODO : We currently still have the issues of the last calculations falling off ie.: Having less SecurityHistory elemnts than previous windows.
    private suspend fun years(
        cycle: TimePeriod,
        listOfSecurityIdentifiers: List<SecurityIdentifier>
    ): Pair<TimePeriod, EvaluationStatistics>? = coroutineScope {
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

        val source = listOfSecurityIdentifiers
            .map { securityIdentifier ->
                async {
                    val securityHistory = m_Provider.getBySecurityIdentifier(securityIdentifier).getOrThrow()
                        .sortedBy { security -> security.timestamp }

                    val sourcesForASecurity = (startYear..endYear - windowSizeYears step m_WindowStepYears)
                        .mapNotNull { year ->
                            val startDate = Instant.parse("${year}-01-01T00:00:00Z")
                            val endDate = Instant.parse("${year + windowSizeYears}-01-01T00:00:00Z")

                            runBackTester(
                                securityHistory.filter { security -> security.timestamp in startDate..endDate },
                                securityIdentifier,
                                startDate,
                                endDate
                            )
                        }

                    Pair(securityIdentifier, sourcesForASecurity)
                }
            }
            .awaitAll()
            .toMap()

        val evaluationStatistic = EvaluationStatistics(
            tradingAlgorithmType = m_TradingAlgorithmType,
            taxation = m_TaxationType,
            startingCapital = m_StartingCapital,
            source = source,
        )

        return@coroutineScope Pair(cycle, evaluationStatistic)
    }

    //===========================================================//

    private fun runBackTester(
        securityHistory: List<SecurityHistory>,
        securityIdentifier: SecurityIdentifier,
        startDate: Instant,
        endDate: Instant
    ): EvaluationStatistics.Source? {
        val out = TradingAlgorithmBackTester(
            history = securityHistory,
            type = m_TradingAlgorithmType,
            securityIdentifier = securityIdentifier,
            startingCapital = m_StartingCapital,
            taxation = m_TaxationType,
            from = startDate,
            to = endDate
        ).runBackTest()

        return if (out.tradeWinrate.isNaN() || out.sharpeRatio.isNaN()) null else out.toSource()
    }

    //===========================================================//
    //===========================================================//
    // Constructor(s)

    constructor(
        provider: IHistoricalMarketDataProvider,
        tradingAlgorithmType: TradingAlgorithm.Type,
        capital: Double,
        taxation: Taxation.Type? = null,
        evaluationStartYear: Instant = Instant.parse("2015-01-01T00:00:00Z"),
        evaluationEndYear: Instant = Instant.parse("2025-01-01T00:00:00Z"),
        windowStepYears: Int = 1
    ) {
        m_Provider = provider
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

    data class Output(
        val list: List<Pair<TimePeriod, EvaluationStatistics>>
    ) {
        private val availablePeriods = TimePeriod.entries.filter { period -> list.any { it.first == period } }

        fun display() {
            require(list.isNotEmpty()) { "No evaluation results available." }

            val first = list.first().second
            val tax = if (first.taxation == null) "Without" else "With"

            println("#===============================================================#")
            println("# Algorithm Evaluation | Algorithm: ${first.tradingAlgorithmType}")
            println("#===============================================================#")
            println("Starting Capital: ${first.startingCapital.format(2)}")
            println("Taxes: $tax")
            println()
            println("M = Mean, TM = Trimmed Mean (middle 60% - excluded Best20 and Worst20), Md = Median")
            println("Best20 = Average of best 20%, Worst20 = Average of worst 20%")
            println()

            metricDisplay(
                title = "Total Capital",
                metricSelector = { it.totalCapital },
                formatter = { it.format(2) }
            )
            metricDisplay(
                title = "CAGR",
                metricSelector = { it.cagr },
                formatter = { "${(it * 100).format(2)}%" }
            )
            metricDisplay(title = "Max Drawdown",
                metricSelector = { it.maxDrawdown },
                formatter = { "${(it * 100).format(2)}%" },
                lowerIsBetter = true
            )
            metricDisplay(
                title = "Sharpe",
                metricSelector = { it.sharpe },
                formatter = { it.format(2) }
            )
            metricDisplay(
                title = "Calmar",
                metricSelector = { it.calmar },
                formatter = { it.format(2) }
            )
        }

        //===========================================================//

        private fun metricDisplay(
            title: String,
            metricSelector: (EvaluationStatistics.Source) -> Double,
            formatter: (Double) -> String,
            lowerIsBetter: Boolean = false
        ) {
            header(title)
            row(
                label = "M",
                value = { it.calculateMetric(metricSelector).mean },
                formatter = formatter
            )
            row(
                label = "TM",
                value = { it.calculateMetric(metricSelector).trimmedMean },
                formatter = formatter
            )
            row(
                label = "Md",
                value = { it.calculateMetric(metricSelector).median },
                formatter = formatter
            )
            row(
                label = "Best20",
                value = {
                    val metric = it.calculateMetric(metricSelector)
                    if(lowerIsBetter) metric.b20 else metric.t20
                },
                formatter = formatter
            )
            row(
                label = "Worst20",
                value = {
                    val metric = it.calculateMetric(metricSelector)
                    if(lowerIsBetter) metric.t20 else metric.b20
                },
                formatter = formatter
            )
            println("-".repeat(14 + availablePeriods.size * 13))
            println()
        }

        //===========================================================//

        private fun header(title: String) {
            println(title)

            val periods = availablePeriods.joinToString(" ") {
                "| ${it.toString().padStart(10)}"
            }

            println("| ${"Statistic".padEnd(10)} $periods |")
            println("-".repeat(14 + availablePeriods.size * 13))
        }

        //===========================================================//

        private fun row(
            label: String,
            value: (EvaluationStatistics) -> Double,
            formatter: (Double) -> String
        ) {
            val byPeriod = list.associate { it.first to it.second }

            val values = availablePeriods.joinToString(" ") { period ->
                "| ${
                    byPeriod[period]
                        ?.let { formatter(value(it)).padStart(10) }
                        ?: " ".repeat(10)
                }"
            }

            println("| ${label.padEnd(10)} $values |")
        }

        //===========================================================//

        private fun rankedLists(
            title: String,
            values: (EvaluationStatistics) -> List<Pair<SecurityIdentifier, Double>>,
            formatter: (Double) -> String
        ) {
            val byPeriod = list.associate { it.first to it.second }

            availablePeriods.forEach { period ->
                val statistics = byPeriod[period] ?: return@forEach

                println("$title | $period")
                println("-".repeat(50))

                values(statistics).forEachIndexed { index, item ->
                    println(
                        "${(index + 1).toString().padStart(2)}. " +
                                "${item.first.tickerSymbol.padEnd(10)} " +
                                formatter(item.second)
                    )
                }

                println()
            }
        }

        //===========================================================//

        private fun Map<SecurityIdentifier, List<Double>>.bestList(lowerIsBetter: Boolean = false): List<Pair<SecurityIdentifier, Double>> {
            val ret = map { (security, values) ->
                Pair(security, values.average())
            }

            return if (lowerIsBetter) {
                ret.sortedBy { security ->
                    security.second
                }
            } else {
                ret.sortedByDescending {security ->
                    security.second
                }
            }
        }

        //===========================================================//

        private fun Map<SecurityIdentifier, List<Double>>.worstList(lowerIsBetter: Boolean = false): List<Pair<SecurityIdentifier, Double>> {
            val ret = map { (security, values) ->
                Pair(security, values.average())
            }

            return if (lowerIsBetter) {
                ret.sortedByDescending { security ->
                    security.second
                }
            } else {
                ret.sortedBy {security ->
                    security.second
                }
            }
        }

        //===========================================================//

        fun getBestList(
            size: Int = 0,
            metric: Metric = Metric.TOTAL_CAPITAL
        ): List<SecurityIdentifier> {

            val statistics = list
                .firstOrNull()
                ?.second
                ?: return emptyList()

            val values = when (metric) {
                Metric.TOTAL_CAPITAL -> statistics.source.mapValues { (_, converted) -> converted.map { it.totalCapital } }.bestList()
                Metric.CAGR -> statistics.source.mapValues { (_, converted) -> converted.map { it.cagr } }.bestList()
            }

            if(size == 0) return values.map{ it.first }
            return values.take(size).map { it.first }
        }

        //===========================================================//

        enum class Metric {
            TOTAL_CAPITAL,
            CAGR
        }
    }

    //===========================================================//

    data class EvaluationStatistics(
        val tradingAlgorithmType: TradingAlgorithm.Type,
        val taxation: Taxation.Type?,
        val startingCapital: Double,
        val source: Map<SecurityIdentifier, List<Source>>,
    ) {
        fun calculateMetric(
            selector: (Source) -> Double
        ): Metric {
            val trim = 0.2
            val selected = source.mapValues { (_, converted) -> converted.map(selector) }

            return Metric(
                mean = selected.values.flatten().average(),
                trimmedMean = selected.values.flatten().trim(trim).average(),
                median = selected.values.flatten().median(),
                t20 = selected.map { (_, list) -> list.average() }.top(trim).average(),
                b20 = selected.map { (_, list) -> list.average() }.bottom(trim).average()
            )
        }

        //===========================================================//
        //===========================================================//
        // Helper Class(es)

        data class Metric(
            val mean: Double,
            val trimmedMean: Double,
            val median: Double,
            val t20: Double,
            val b20: Double
        )

        //===========================================================//

        data class Source(
            val from: Instant,
            val to: Instant,
            val startingCapital: Double,

            val totalCapital: Double,
            val cagr: Double,
            val maxDrawdown: Double,
            val sharpe: Double,
            val calmar: Double,

            val totalBuysMade: Double,
            val totalSellsMade: Double,
            val forceClosedTrades: Double,
            val tradeWinrate: Double,

            val averageWin: Double,
            val averageLoss: Double,

            val stockHistory: List<SecurityHistory>,
            val tradingOrders: List<TradingOrder>,
        )
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
        data object Year3 : TimePeriod {
            override fun toString(): String = "3 Year"
            override fun toInt(): Int = 3
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
                Year3,
                Year2,
                Year1
            )
        }

        fun toInt(): Int
    }

    //===========================================================//
    //===========================================================//
    // Extension(s)

    private fun TradingAlgorithmBackTester.Output.toSource(): EvaluationStatistics.Source {
        val years = Duration.between(from.toJavaInstant(), to.toJavaInstant()).toDays().toDouble() / 365.2425
        val cagr = ((totalCapital / startingCapital).pow(1.0 / years) - 1.0)
        val calmar =
            if (maxDrawdown == 0.0) if (cagr > 0.0) Double.POSITIVE_INFINITY else 0.0
            else cagr / abs(maxDrawdown)

        return EvaluationStatistics.Source(
            from,
            to,
            startingCapital,

            totalCapital,
            cagr,
            maxDrawdown,
            sharpeRatio,
            calmar,

            totalBuysMade.toDouble(),
            totalSellsMade.toDouble(),
            forceClosedTrades.toDouble(),
            tradeWinrate,

            averageWin,
            averageLoss,
            stockHistory,
            tradingOrders,
        )
    }
}