package domain.trader

import application.signal.SignalGenerator
import domain.algorithm.TradingAlgorithm
import domain.assets.Quote
import domain.assets.security.SecurityHolding
import domain.assets.security.SecurityIdentifier
import domain.signal.TradingSignal
import java.util.UUID

//===========================================================//
/**
 * Represents a virtual trader that is responsible for one security
 *
 * The trader owns allocated capital, current holding batches and activate
 * trading algorithm that is (will be) decided by the Algorithm Manager.
 * It only created trading signals based on given quote
 *
 * @param id the unique trader id
 * @param stockName the display name of the stock handled by the trader
 * @param securityIdentifier identifier of the traded security
 * @param m_AllocatedCapital the capital currently allocated by the trader
 * @param m_Holdings holding batches of this security
 * @param m_Algorithm currently used trading algorithm
 */
// ===========================================================//

class Trader(
    val id: UUID,
    val stockName: String,
    val securityIdentifier: SecurityIdentifier,
    private var m_AllocatedCapital: Double,
    private var m_Holdings: MutableList<SecurityHolding>,
    private var m_Algorithm: TradingAlgorithm,
    private var m_SignalGenerator: SignalGenerator = SignalGenerator()
) {
    init {
        require(!stockName.isBlank()) {"StockName"}
        require(m_AllocatedCapital >= 0.0) {"AllocatedCapital"}
    }

    //===========================================================//
    //===========================================================//
    // Public Method(es)


    fun createSignals(quote: Quote): List<TradingSignal>{
        val currentPrice = quote.currentPrice

        val output = m_Algorithm.run(
            m_Holdings,
            m_AllocatedCapital,
            currentPrice,
        )

        var stockCount = getCurrentStockCount()

        if(output.buy != null) stockCount += output.buy.amount

        if(output.sell != null) stockCount -= getSellAmount(output.sell)

        return m_SignalGenerator.createSignal(
            output,
            m_AllocatedCapital,
            currentPrice,
            stockCount
        )
    }

    //===========================================================//

    fun getCurrentStockCount(): Long {
        var ret = 0L
        m_Holdings.forEach { ret += it.amount }
        return ret
    }

    //===========================================================//

    fun changeAlgorithm(newAlgorithm: TradingAlgorithm){
        m_Algorithm = newAlgorithm
    }

    //===========================================================//

    fun getAllocatedCapital(): Double {
        return m_AllocatedCapital
    }

    //===========================================================//

    fun getHoldings(): List<SecurityHolding> {
        return m_Holdings.toList()
    }

    //===========================================================//
    //===========================================================//
    // Private Method(es)

    private fun getSellAmount(sell: TradingAlgorithm.Output.Sell): Long {
        var ret = 0L
        sell.batches.forEach{ ret += it.second }
        return ret
    }
}