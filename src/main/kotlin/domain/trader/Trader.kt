package domain.trader

import application.signal.SignalGenerator
import domain.algorithm.TradingAlgorithm
import domain.assets.Quote
import domain.assets.security.SecurityHolding
import domain.assets.security.SecurityIdentifier
import domain.signal.TradingSignal
import java.util.UUID

class Trader(
    val id: UUID,
    val stockName: String,
    val securityIdentifier: SecurityIdentifier,
    private var m_allocatedCapital: Double,
    private var m_holdings: MutableList<SecurityHolding>,
    private var m_algorithm: TradingAlgorithm,
    private var m_signalGenerator: SignalGenerator = SignalGenerator()
) {

    fun createSignal(quote: Quote): List<TradingSignal>{
        val currentPrice = quote.currentPrice

        val output = m_algorithm.run(
            m_holdings,
            m_allocatedCapital,
            currentPrice,
        )

        return m_signalGenerator.createSignal(
            stockName,
            output,
            m_allocatedCapital,
            currentPrice,
            m_holdings.sumOf{
                it.amount
            }
        )
    }

    fun changeAlgorithm(newAlgorithm: TradingAlgorithm){
        m_algorithm = newAlgorithm
    }

    fun getAllocatedCapital(): Double {
        return m_allocatedCapital
    }

    fun getHoldings(): List<SecurityHolding> {
        return m_holdings.toList()
    }
}