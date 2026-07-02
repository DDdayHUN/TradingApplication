package domain

import application.backtest.TradingAlgorithmBackTester
import domain.algorithm.TradingAlgorithm
import domain.assets.security.SecurityIdentifier
import domain.tax.ITaxation
import infrastructure.network.finnhub.FinnhubTester

suspend fun main() {
    val BT = TradingAlgorithmBackTester(
        ITaxation.HUNGARY,
        TradingAlgorithm.Type.TACPP46,
        10000.0,
        "Cloudflare",
        20,
        24)
    BT.runBackTest()

    /*
    val FN = FinnhubTester()
    FN.runFinnhubTester(
        SecurityIdentifier(
            "US0378331005",
            "USD",
            "Apple"
        )
    )
    */
}