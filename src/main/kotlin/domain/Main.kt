package domain

import application.backtest.AlgorithmBackTester
import domain.algorithm.Algorithm
import domain.tax.Taxation
import infrastructure.network.finnhub.FinnhubTester

fun main() {
    val BT = AlgorithmBackTester(Taxation.HUNGARY, Algorithm.Type.TACPP46, 10000.0, "Cloudflare", 20, 24)
    BT.runBackTest()
}