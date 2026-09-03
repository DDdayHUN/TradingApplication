package application

import domain.algorithm.TradingAlgorithm
import domain.interfaces.IHistoricalMarketDataProvider
import domain.market.security.SecurityHolding
import domain.market.security.SecurityIdentifier
import domain.trader.TradingOrder
import java.time.Instant
import java.util.UUID

//===========================================================//
/**
 * Provides a simple interface to manually execute a trading algorithm
 * using the current market price, available capital and existing holdings.
 *
 * The class creates the selected trading algorithm, executes it once,
 * converts the result into a trading order, and prints a human-readable
 * summary to the console.
 */
//===========================================================//

@Deprecated("Not used")
class ManualTrading {
    //===========================================================//
    //===========================================================//
    // Private Field(s)

    private val m_Provider: IHistoricalMarketDataProvider
    private val m_AlgorithmType: TradingAlgorithm.Type
    private val m_SecurityIdentifier: SecurityIdentifier

    private val m_AllocatedCapital: Double
    private val m_Holdings: Set<SecurityHolding>

    //===========================================================//
    //===========================================================//
    // Public Method(es)

    fun run(currentPrice: Double) {
        val alg = TradingAlgorithm.create(
            provider = m_Provider,
            type = m_AlgorithmType,
            securityIdentifier = m_SecurityIdentifier
        )
        val output = alg.run(m_Holdings, m_AllocatedCapital, currentPrice)
        val text = output.toTradingOrder(currentPrice).toReadableText()
        println("#===============================================================#")
        println("# Manual Trader | Algorithm: $m_AlgorithmType")
        println("#===============================================================#")
        println("Stock: ${m_SecurityIdentifier.tickerSymbol}")
        println("Available Capital: $m_AllocatedCapital")
        println("Current Price: $currentPrice")
        println()
        println("Order: $text")
    }

    //===========================================================//
    //===========================================================//
    // Constructor(s)

    constructor(
        provider: IHistoricalMarketDataProvider,
        algorithm: TradingAlgorithm.Type,
        securityIdentifier: SecurityIdentifier,
        allocatedCapital: Double
    ) {
        m_Provider = provider
        m_AlgorithmType = algorithm
        m_SecurityIdentifier = securityIdentifier
        m_AllocatedCapital = allocatedCapital
        m_Holdings = HashSet()
    }

    //===========================================================//
    //===========================================================//
    // Extension(s)

    private fun TradingAlgorithm.Output.toTradingOrder(currentPrice: Double): TradingOrder {
        return TradingOrder(
            UUID.randomUUID(),
            UUID.randomUUID(),
            m_SecurityIdentifier,
            buy,
            sell,
            currentPrice,
            Instant.now()
        )
    }
}