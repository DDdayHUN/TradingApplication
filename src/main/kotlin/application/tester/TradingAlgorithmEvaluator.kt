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
        val avgTotalCapital = list.map { it.totalCapital }.average()
        val avgBuysMade = list.map { it.totalBuysMade }.average()
        val avgSellsMade = list.map { it.totalSellsMade }.average()
        val avgForceClosedTrades = list.map { it.forceClosedTrades }.average()
        val avgTotalWins = list.map { it.tradeWinrate }.average()
        val avgMaxDrawdown = list.map { it.maxDrawdown }.average()

        val avgTop1PercentMaxDrawdown = list
            .map { it.maxDrawdown }
            .sortedDescending()
            .let { sorted ->
                val count = maxOf(1, (sorted.size * 0.01).toInt())
                sorted.take(count).average()
            }

        val avgSharpeRatio = list.map { it.sharpeRatio }.average()
        val avgYearlyPercentChangeOfCapital = list.map { it.cagr }.average()

        return AverageOutput(
            m_TradingAlgorithmType,
            m_TaxationType,
            m_StartingCapital,
            avgTotalCapital,
            avgBuysMade,
            avgSellsMade,
            avgForceClosedTrades,
            avgTotalWins,
            avgMaxDrawdown,
            avgTop1PercentMaxDrawdown,
            avgSharpeRatio,
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

    data class Output(val list: List<Pair<AverageOutput, TimePeriod>>) {
        /**
         * Displays the algorithm evaluation results.
         *
         * Column definitions:
         *
         * Period:
         *   The evaluation window length used for the backtest (1, 2, 5, or 10 years).
         *
         * Final Capital:
         *   The average ending portfolio value after all backtests in this period.
         *
         * Profit:
         *   The average absolute profit:
         *   Final Capital - Starting Capital.
         *
         * Profit %:
         *   The average total return percentage:
         *   (Final Capital - Starting Capital) / Starting Capital.
         *
         * CAGR %:
         *   The average annualized return (CAGR):
         *   The yearly compounded growth rate over the evaluation period.
         *
         * Buys:
         *   The average number of buy orders executed during a backtest.
         *
         * Sells:
         *   The average number of sell orders executed during a backtest.
         *
         * Forced C. (Forced Closures):
         *   The average number of trades closed forcibly by the backtester
         *   (for example, closing remaining positions at the end of the test).
         *
         * Winrate:
         *   The average percentage of profitable trades:
         *   Winning Trades / Total Closed Trades.
         *
         * Max DD (Max Drawdown):
         *   The average maximum peak-to-trough capital loss observed during
         *   each backtest.
         *
         * Top1% M.DD (Top1% Max Drawdown):
         *   The average maximum drawdown of the worst 1% performing backtests.
         *   This represents tail risk and shows how badly the algorithm performs
         *   during its worst market conditions.
         *
         * Sharpe:
         *   The average Sharpe ratio, representing risk-adjusted return.
         *   Higher values indicate better return relative to volatility.
         */
        fun display() {

            require(list.isNotEmpty()) { "No evaluation results available." }

            val firstElement = list.first().first
            val tax = if(firstElement.taxation == null) "Without" else "With"
            println("#===============================================================#")
            println("# Algorithm Evaluation | Algorithm: ${firstElement.tradingAlgorithmType}")
            println("#===============================================================#")
            println("Starting Capital: ${firstElement.startingCapital.format(2)}")
            println("Taxes: $tax")
            println()
            // NOTE: YEAH...that was not me xd, wtf is this gpt
            println("All subsequent values after Period are averages.")
            println(
                "| ${"Period".padEnd(7)} " +
                "| ${"Final Capital".padStart(13)} " +
                "| ${"Profit".padStart(10)} " +
                "| ${"Profit %".padStart(8)} " +
                "| ${"CAGR %".padStart(8)} " +
                "| ${"Buys".padStart(6)} " +
                "| ${"Sells".padStart(6)} " +
                "| ${"Forced C.".padStart(9)} " +
                "| ${"Winrate".padStart(7)} " +
                "| ${"Max DD".padStart(6)} " +
                "| ${"Worst1% M.DD".padStart(12)} " +
                "| ${"Sharpe".padStart(6)} " +
                "| ${"Calmar".padStart(6)} " +
                "| ${"Worst1% Calmar".padStart(14)} |"
            )

            println("-".repeat(161))

            list.forEach {
                val average = it.first
                val period = it.second

                val profit = average.totalCapital - average.startingCapital
                val profitPercent = (profit / average.startingCapital) * 100.0

                val calmar = average.cagr / average.maxDrawdown
                val calmarTop1Percent = average.cagr / average.top1PercentDrawdown

                println(
                    "| ${period.toString().padEnd(7)} " +
                    "| ${average.totalCapital.format(2).padStart(13)} " +
                    "| ${profit.format(2).padStart(10)} " +
                    "| ${"${profitPercent.format(2)}%".padStart(8)} " +
                    "| ${"${average.cagr.times(100.0).format(2)}%".padStart(8)} " +
                    "| ${average.totalBuysMade.format(2).padStart(6)} " +
                    "| ${average.totalSellsMade.format(2).padStart(6)} " +
                    "| ${average.forceClosedTrades.format(2).padStart(9)} " +
                    "| ${"${average.tradeWinrate.times(100.0).format(2)}%".padStart(7)} " +
                    "| ${"${average.maxDrawdown.times(100.0).format(2)}%".padStart(6)} " +
                    "| ${"${average.top1PercentDrawdown.times(100.0).format(2)}%".padStart(12)} " +
                    "| ${average.sharpeRatio.format(2).padStart(6)} " +
                    "| ${calmar.format(2).padStart(6)} " +
                    "| ${calmarTop1Percent.format(2).padStart(14)} |"
                )
            }

            println()
        }
    }

    //===========================================================//

    data class AverageOutput(
        val tradingAlgorithmType: TradingAlgorithm.Type,
        val taxation: Taxation.Type?,
        val startingCapital: Double,
        val totalCapital: Double,
        val totalBuysMade: Double,
        val totalSellsMade: Double,
        val forceClosedTrades: Double,
        val tradeWinrate: Double,
        val maxDrawdown: Double,
        val top1PercentDrawdown: Double,
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

        fun toInt(): Int
    }

    //===========================================================//
    //===========================================================//
    // Extension(s)

    private fun TradingAlgorithmBackTester.Output.toAverageOutput(): AverageOutput {
        val years = java.time.Duration.between(from.toJavaInstant(), to.toJavaInstant()).toDays().toDouble() / 365.2425
        val cagr = ((totalCapital / startingCapital).pow(1.0 / years) - 1.0)

        return AverageOutput(
            tradingAlgorithmType,
            taxation,
            startingCapital,
            totalCapital,
            totalBuysMade.toDouble(),
            totalSellsMade.toDouble(),
            forceClosedTrades.toDouble(),
            tradeWinrate,
            maxDrawdown,
            maxDrawdown, // We set this later
            sharpeRatio,
            cagr
        )
    }
}