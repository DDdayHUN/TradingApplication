package application.tester

import domain.algorithm.TradingAlgorithm
import domain.tax.ITaxation

class TradingAlgorithmEvaulater {
    //===========================================================//
    //===========================================================//
    // Private Field(s)

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

    private fun init() {
        m_TradingAlgorithmBackTester = TradingAlgorithmBackTester(m_Taxation, m_TradingAlgorithmType)
    }

    //===========================================================//
    //===========================================================//
    // Constructor(s)

    constructor(taxation: ITaxation, tradingAlgorithmType: TradingAlgorithm.Type) {
        m_Taxation = taxation
        m_TradingAlgorithmType = tradingAlgorithmType
    }
}