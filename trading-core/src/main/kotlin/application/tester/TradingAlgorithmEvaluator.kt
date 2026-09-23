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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import java.time.Duration
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

    private val m_Provider: IHistoricalMarketDataProvider
    private val m_TradingAlgorithmType: TradingAlgorithm.Type
    private val m_TaxationType: Taxation.Type?
    private val m_StartingCapital: Double

    private val m_EvaluationStartDate: Instant
    private val m_EvaluationEndDate: Instant
    private val m_WindowStepYears: Int

    private val backtestDispatcher =
        Dispatchers.Default.limitedParallelism(
            Runtime.getRuntime().availableProcessors()
        )

    //===========================================================//
    //===========================================================//
    // Public Method(es)

    suspend fun runEvaluation(securityIdentifiers: List<SecurityIdentifier> = listOf()): Output = coroutineScope {
        val listOfSecurityIdentifiers =
            if (securityIdentifiers.isEmpty()) {
                m_Provider.getAllSecurityIdentifiers().getOrThrow()
            } else {
                securityIdentifiers
            }

        val timePeriods = listOf(
            TimePeriod.Year10,
            TimePeriod.Year5,
            TimePeriod.Year3,
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

    private suspend fun years(
        cycle: TimePeriod,
        listOfSecurityIdentifiers: List<SecurityIdentifier>
    ): Pair<Map<SecurityIdentifier, List<TradingAlgorithmBackTesterOutputConverted>>, TimePeriod>? = coroutineScope {
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

        val out = (startYear..endYear - windowSizeYears step m_WindowStepYears)
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

        if (out.isEmpty()) return@coroutineScope null // TODO : Nem tudom hogy kell-e ez.

        val ret = out.groupBy { it.securityIdentifier }

        return@coroutineScope Pair(ret, cycle)
    }

    //===========================================================//

    private suspend fun runBackTesters(
        listOfSecurityIdentifiers: List<SecurityIdentifier>,
        startDate: Instant,
        endDate: Instant
    ): List<TradingAlgorithmBackTesterOutputConverted> = coroutineScope {
        val outputs = listOfSecurityIdentifiers.map { securityIdentifier ->
            async(backtestDispatcher) {
                val out = TradingAlgorithmBackTester(
                    provider = m_Provider,
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

        return@coroutineScope outputs.filterNotNull().map { it.toConvertedOutput() }
    }

    //===========================================================//

    private fun calculateStatistics(
        map: Map<SecurityIdentifier, List<TradingAlgorithmBackTesterOutputConverted>>
    ): EvaluationStatistics {
        val trim = 0.2

        // Statistics gathered and mapped to Map<SecurityIdentifier, VALUE>
        val capitals = map.mapValues { (_, converted) -> converted.map { it.totalCapital } }
        val cagrs = map.mapValues { (_, converted) -> converted.map { it.cagr } }
        val sharpes = map.mapValues { (_, converted) -> converted.map { it.sharpeRatio } }
        val drawdowns = map.mapValues { (_, converted) -> converted.map { it.maxDrawdown } }
        val calmar = map.mapValues { (_, converted) ->
            converted
                .filter { it.maxDrawdown != 0.0 }
                .map { it.cagr / it.maxDrawdown }
        }

        val capitalBest20 = capitals.bestList()
        val capitalWorst20 = capitals.worstList()

        val cagrBest20 = cagrs.bestList()
        val cagrWorst20 = cagrs.worstList()

        return EvaluationStatistics(
            tradingAlgorithmType = m_TradingAlgorithmType,
            taxation = m_TaxationType,
            startingCapital = m_StartingCapital,

            totalCapitalMean = capitals.values.flatten().average(),
            totalCapitalTrimmedMean = capitals.values.flatten().trim(trim).average(),
            totalCapitalMedian = capitals.values.flatten().median(),
            totalCapitalT20 = capitals
                .map { (_, list) -> list.average() }
                .top(trim).average(),
            totalCapitalB20 = capitals
                .map { (_, list) -> list.average() }
                .bottom(trim).average(),

            cagrMean = cagrs.values.flatten().average(),
            cagrTrimmedMean = cagrs.values.flatten().trim(trim).average(),
            cagrMedian = cagrs.values.flatten().median(),
            cagrT20 = cagrs
                .map { (_, list) -> list.average() }
                .top(trim).average(),
            cagrB20 = cagrs
                .map { (_, list) -> list.average() }
                .bottom(trim).average(),

            sharpeMean = sharpes.values.flatten().average(),
            sharpeTrimmedMean = sharpes.values.flatten().trim(trim).average(),
            sharpeMedian = sharpes.values.flatten().median(),
            sharpeT20 = sharpes
                .map { (_, list) -> list.average() }
                .top(trim).average(),
            sharpeB20 = sharpes
                .map { (_, list) -> list.average() }
                .bottom(trim).average(),

            maxDrawdownMean = drawdowns.values.flatten().average(),
            maxDrawdownTrimmedMean = drawdowns.values.flatten().trim(trim).average(),
            maxDrawdownMedian = drawdowns.values.flatten().median(),
            maxDrawdownT20 = drawdowns
                .map { (_, list) -> list.average() }
                .bottom(trim).average(),
            maxDrawdownB20 = drawdowns
                .map { (_, list) -> list.average() }
                .top(trim).average(),

            calmarMean = calmar.values.flatten().average(),
            calmarTrimmedMean = calmar.values.flatten().trim(trim).average(),
            calmarMedian = calmar.values.flatten().median(),
            calmarT20 = calmar
                .map { (_, list) -> list.average() }
                .top(trim).average(),
            calmarB20 = calmar
                .map { (_, list) -> list.average() }
                .bottom(trim).average(),
            totalCapitalBest20 = capitalBest20,
            totalCapitalWorst20 = capitalWorst20,

            cagrBest20 = cagrBest20,
            cagrWorst20 = cagrWorst20,
            )
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

    data class Output(val list: List<Pair<EvaluationStatistics, TimePeriod>>) {
        private val availablePeriods: List<TimePeriod> get() = TimePeriod.entries.filter { period -> list.any { it.second == period } }

        fun getBestList(
            size: Int = 20,
            metric: Metric = Metric.TOTAL_CAPITAL
        ): List<SecurityIdentifier> {

            val statistics = list
                .firstOrNull()
                ?.first
                ?: return emptyList()

            val values = when (metric) {
                Metric.TOTAL_CAPITAL -> statistics.totalCapitalBest20
                Metric.CAGR -> statistics.cagrBest20
            }

            return values
                .take(size)
                .map { it.first }
        }

        fun getWorstList(
            size: Int = 20,
            metric: Metric = Metric.TOTAL_CAPITAL
        ): List<Pair<SecurityIdentifier, Double>> {

            val statistics = list
                .firstOrNull()
                ?.first
                ?: return emptyList()

            val values = when (metric) {
                Metric.TOTAL_CAPITAL -> statistics.totalCapitalWorst20
                Metric.CAGR -> statistics.cagrWorst20
            }

            return values.take(size)
        }

        enum class Metric {
            TOTAL_CAPITAL,
            CAGR
        }

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
            println("M = Mean, TM = Trimmed Mean (middle 60% - excluded Best20 and Worst20), Md = Median")
            println("Best20 = Average of best 20%, Worst20 = Average of worst 20%")
            println()

            // Total Capital
            header("Total Capital")
            row("M",     { it.totalCapitalMean },        { it.format(2) })
            row("TM",    { it.totalCapitalTrimmedMean }, { it.format(2) })
            row("Md",    { it.totalCapitalMedian },      { it.format(2) })
            row("Best20", { it.totalCapitalT20 },         { it.format(2) })
            row("Worst20", { it.totalCapitalB20 },         { it.format(2) })
            println("-".repeat(14 + availablePeriods.size * 13))
            println()

            // CAGR
            header("CAGR")
            row("M",     { it.cagrMean },        { "${(it * 100).format(2)}%" })
            row("TM",    { it.cagrTrimmedMean }, { "${(it * 100).format(2)}%" })
            row("Md",    { it.cagrMedian },      { "${(it * 100).format(2)}%" })
            row("Best20", { it.cagrT20 },         { "${(it * 100).format(2)}%" })
            row("Worst20", { it.cagrB20 },         { "${(it * 100).format(2)}%" })
            println("-".repeat(14 + availablePeriods.size * 13))
            println()

            // Max Drawdown
            header("Max Drawdown")
            row("M",     { it.maxDrawdownMean },        { "${(it * 100).format(2)}%" })
            row("TM",    { it.maxDrawdownTrimmedMean }, { "${(it * 100).format(2)}%" })
            row("Md",    { it.maxDrawdownMedian },      { "${(it * 100).format(2)}%" })
            row("Best20", { it.maxDrawdownT20 },         { "${(it * 100).format(2)}%" })
            row("Worst20", { it.maxDrawdownB20 },         { "${(it * 100).format(2)}%" })
            println("-".repeat(14 + availablePeriods.size * 13))
            println()

            // Sharpe
            header("Sharpe")
            row("M",     { it.sharpeMean },        { it.format(2) })
            row("TM",    { it.sharpeTrimmedMean }, { it.format(2) })
            row("Md",    { it.sharpeMedian },      { it.format(2) })
            row("Best20", { it.sharpeT20 },         { it.format(2) })
            row("Worst20", { it.sharpeB20 },         { it.format(2) })
            println("-".repeat(14 + availablePeriods.size * 13))
            println()

            // Calmar
            header("Calmar")
            row("M",     { it.calmarMean },        { it.format(2) })
            row("TM",    { it.calmarTrimmedMean }, { it.format(2) })
            row("Md",    { it.calmarMedian },      { it.format(2) })
            row("Best20", { it.calmarT20 },         { it.format(2) })
            row("Worst20", { it.calmarB20 },         { it.format(2) })
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
            val byPeriod = list.associate { it.second to it.first }

            val values = availablePeriods.joinToString(" ") { period ->
                "| ${
                    byPeriod[period]
                        ?.let { formatter(value(it)).padStart(10) }
                        ?: " ".repeat(10)
                }"
            }

            println("| ${label.padEnd(10)} $values |")
        }

        private fun rankedLists(
            title: String,
            values: (EvaluationStatistics) -> List<Pair<SecurityIdentifier, Double>>,
            formatter: (Double) -> String
        ) {
            val byPeriod = list.associate { it.second to it.first }

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

        val totalCapitalBest20: List<Pair<SecurityIdentifier,Double>>,
        val totalCapitalWorst20: List<Pair<SecurityIdentifier, Double>>,

        val cagrMean: Double,
        val cagrTrimmedMean: Double,
        val cagrMedian: Double,
        val cagrT20: Double,
        val cagrB20: Double,

        val cagrBest20: List<Pair<SecurityIdentifier,Double>>,
        val cagrWorst20: List<Pair<SecurityIdentifier,Double>>,

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
        val securityIdentifier: SecurityIdentifier,
        val duration: Duration,
        val startingCapital: Double,
        val totalCapital: Double,

        val cagr: Double,

        val totalBuysMade: Double,
        val totalSellsMade: Double,
        val forceClosedTrades: Double,
        val tradeWinrate: Double,
        val maxDrawdown: Double,
        val sharpeRatio: Double,

        val averageWin: Double,
        val averageLoss: Double,

        val stockHistory: List<SecurityHistory>,
        val tradingOrders: List<TradingOrder>
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

    private fun TradingAlgorithmBackTester.Output.toConvertedOutput(): TradingAlgorithmBackTesterOutputConverted {
        val years = Duration.between(from.toJavaInstant(), to.toJavaInstant()).toDays().toDouble() / 365.2425
        val cagr = ((totalCapital / startingCapital).pow(1.0 / years) - 1.0)
        val duration = Duration.between(from.toJavaInstant(), to.toJavaInstant())!!

        return TradingAlgorithmBackTesterOutputConverted(
            tradingAlgorithmType,
            taxation,
            securityIdentifier,
            duration,
            startingCapital,
            totalCapital,
            cagr,
            totalBuysMade.toDouble(),
            totalSellsMade.toDouble(),
            forceClosedTrades.toDouble(),
            tradeWinrate,
            maxDrawdown,
            sharpeRatio,
            averageWin,
            averageLoss,
            stockHistory,
            tradingOrders
        )
    }

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
}