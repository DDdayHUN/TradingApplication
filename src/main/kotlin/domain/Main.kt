package domain

import application.backtest.AlgorithmBackTester
import domain.algorithm.TradingAlgorithm
import domain.assets.security.SecurityIdentifier
import domain.tax.ITaxation
import infrastructure.network.finnhub.FinnhubTester

fun main() {
    val BT = AlgorithmBackTester(
        ITaxation.HUNGARY,
        TradingAlgorithm.Type.TACPP46,
        3000.0,
        "Apple",
        20,
        24)
    BT.runBackTest()

    val FN = FinnhubTester()
    FN.runFinnhubTester(SecurityIdentifier(
                        "US0378331005",
                        "USD",
                        "Apple"
                        ))
}