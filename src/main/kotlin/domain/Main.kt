package domain

import application.backtest.AlgorithmBackTester
import domain.algorithm.TradingAlgorithm
import domain.tax.ITaxation

fun main() {
    val BT = AlgorithmBackTester(
        ITaxation.HUNGARY,
        TradingAlgorithm.Type.TACPP46,
        10000.0,
        "Cloudflare",
        20,
        24)
    BT.runBackTest()
}