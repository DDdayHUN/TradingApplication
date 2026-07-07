package application.tester

import data.repository.historical_data.HistoricalMarketDataProvider
import domain.algorithm.TradingAlgorithm
import domain.market.security.SecurityIdentifier
import domain.tax.Taxation
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import format
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

    //===========================================================//
    //===========================================================//
    // Public Method(es)

    suspend fun runEvaluation(): Output = coroutineScope {
        val listOfSecurityIdentifiers = HistoricalMarketDataProvider.getAllSecurityIdentifiers()

        val a1 = async { years(TimePeriod.Year10, listOfSecurityIdentifiers) }
        val a2 = async { years(TimePeriod.Year5,listOfSecurityIdentifiers) }
        val a3 = async { years(TimePeriod.Year2,listOfSecurityIdentifiers) }
        val a4 = async { years(TimePeriod.Year1, listOfSecurityIdentifiers) }

        val r = listOf(a1, a2, a3, a4).awaitAll()

        return@coroutineScope Output(r)
    }

    //===========================================================//
    //===========================================================//
    // Private Method(es)

    private suspend fun years(cycle: TimePeriod, listOfSecurityIdentifiers: List<SecurityIdentifier>): Pair<AverageOutput, TimePeriod> = coroutineScope {
        val increment = cycle.toInt()
        val results = (2015 until 2025 step increment).map { currentYear ->
            async {
                val startDate = Instant.parse("${currentYear}-01-01T00:00:00Z")
                val endDate = Instant.parse("${currentYear + increment}-01-01T00:00:00Z")
                averager(runBackTesters(listOfSecurityIdentifiers, startDate, endDate))
            }
        }

        return@coroutineScope Pair(averager(results.awaitAll()), cycle)
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
        taxation: Taxation.Type? = null
    ) {
        m_TradingAlgorithmType = tradingAlgorithmType
        m_StartingCapital = capital
        m_TaxationType = taxation
    }

    //===========================================================//
    //===========================================================//
    // Helper Class(es)

    data class Output(val list: List<Pair<AverageOutput, TimePeriod>>) {
        fun display() {
            val firstElement = list.firstOrNull()?.first ?: error("Empty")
            val tax = if(firstElement.taxation == null) "Without" else "With"

            println("#===============================================================#")
            println("# Algorithm Evaluation | Algorithm: ${firstElement.tradingAlgorithmType}")
            println("#===============================================================#")
            println("Starting Capital: ${firstElement.startingCapital.format(2)}")
            println("Taxes: $tax")
            println("(All subsequent values are averages)")
            println()

            list.forEach {
                println("#===============================================================#")
                println("TimePeriod: ${it.second}")
                println()
                it.first.display()
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
        fun display() {
            val deltaCapital = totalCapital - startingCapital
            val deltaCapitalInPercent = (deltaCapital / startingCapital) * 100.0

            println("Total Capital: ${totalCapital.format(2)}")
            println("Delta Capital: ${deltaCapital.format(2)}")
            println("Percent Change: ${deltaCapitalInPercent.format(2)}%")
            println("Yearly Percent Change: ${yearlyPercentChangeOfCapital.format(2)}%")
            println()

            println("Total Buys Made: ${totalBuysMade.format(2)}")
            println("Total Sells Made: ${totalSellsMade.format(2)}")
            println("Force Closed Trades: ${forceClosedTrades.format(2)}")
            println("Winrate: ${tradeWinrate.format(2)}%")
            println("Sharpe Ratio: ${sharpieRatio.format(2)}")
            println()
        }
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