import application.tester.TradingAlgorithmBackTester
import domain.algorithm.TradingAlgorithm
import domain.assets.security.SecurityIdentifier
import domain.tax.ITaxation

class TradingAlgorithmEvaulater {
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

    private fun init() {
        m_TradingAlgorithmBackTester = TradingAlgorithmBackTester(
            m_Taxation,
            m_TradingAlgorithmType,
            SecurityIdentifier("", "", ""),
            m_Capital
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