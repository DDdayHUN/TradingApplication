package application.tester

import data.HistoricalMarketDataProvider
import domain.algorithm.TradingAlgorithm
import domain.assets.security.SecurityIdentifier
import domain.tax.ITaxation
import kotlin.time.Instant

class TradingAlgorithmEvaluater {
    //===========================================================//
    //===========================================================//
    // Private Field(s)

    private val m_Capital: Double
    private val m_TradingAlgorithmType: TradingAlgorithm.Type

    private val m_Taxation: ITaxation
    private lateinit var m_TradingAlgorithmBackTester: TradingAlgorithmBackTester

    //===========================================================//
    //===========================================================//
    // Public Method(es)

    fun runEvaluation() {

    }

    //===========================================================//
    //===========================================================//
    // Private Method(es)

    suspend private fun init() {
        val allHistory = HistoricalMarketDataProvider.loadAllFromFiles(Instant.DISTANT_PAST, Instant.DISTANT_FUTURE)
        m_TradingAlgorithmBackTester = TradingAlgorithmBackTester(
            type = m_TradingAlgorithmType,
            securityIdentifier = SecurityIdentifier("","", ""),
            startingCapital = m_Capital,
            taxation = m_Taxation,
        )
    }

    //===========================================================//
    //===========================================================//
    // Constructor(s)

    constructor(capital: Double, taxation: ITaxation, tradingAlgorithmType: TradingAlgorithm.Type) {
        m_Capital = capital
        m_Taxation = taxation
        m_TradingAlgorithmType = tradingAlgorithmType
    }
}