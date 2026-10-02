package application.service.trader

import application.logging.logger
import application.provider.MarketDataProvider
import data.repository.trader.ITraderRepository
import domain.algorithm.TradingAlgorithm
import domain.market.Quote
import domain.market.security.SecurityIdentifier
import domain.trader.SellHolding
import domain.trader.TradingOrder
import exception.api.HoldingNotFoundException
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
class TraderService(
    @param:Qualifier("yahoo")
    private val marketDataProvider: MarketDataProvider,
    private val traderRepository: ITraderRepository
) : ITraderService {

    //===========================================================//
    //===========================================================//
    // Private Field(s)

    private val logger = logger<TraderService>()

    //===========================================================//
    //===========================================================//
    // Public Method(s)

    //===========================================================//

    @Transactional
    override suspend fun executeTrader(userId: UUID, traderId: UUID): TradingOrder {
        val trader = traderRepository.getById(traderId).getOrThrow()

        val quote = getCurrentPrice(userId, trader.securityIdentifier)
        //val quote = Quote(540.0)
        val order = trader.createOrder(quote)

        traderRepository.save(trader).getOrThrow()
        return order
    }

    //===========================================================//

    @Transactional
    override suspend fun applyBuyFill(traderId: UUID, filledQuantity: Int, averageFillPrice: Double) {
       val trader = traderRepository.getById(traderId).getOrThrow()

        trader.applyBuyFill(
            price = averageFillPrice,
            amount = filledQuantity
        )

        traderRepository.save(trader).getOrThrow()
    }

    //===========================================================//

    @Transactional
    override suspend fun applySellFill(
        traderId: UUID,
        sellAllocations: Set<SellHolding>,
        averageFillPrice: Double
    ) {
        val trader = traderRepository.getById(traderId).getOrThrow()
        trader.applySellFill(price = averageFillPrice, holdingsToSell = sellAllocations)
        traderRepository.save(trader).getOrThrow()
    }

    //===========================================================//

    @Transactional(readOnly = true)
    override suspend fun forceSellHolding(userId: UUID, traderId: UUID, securityHoldingId: UUID): TradingOrder {
       val trader = traderRepository.getById(traderId).getOrThrow()

        val holding = trader.holdings
            .find { holding -> holding.id == securityHoldingId }
            ?: throw HoldingNotFoundException(securityHoldingId)

        return TradingOrder(
            traderId = trader.id,
            signal = TradingAlgorithm.Output(null, TradingAlgorithm.Output.Sell(setOf(Pair(holding, holding.amount)))),
            atPrice = getCurrentPrice(userId, trader.securityIdentifier).currentPrice,
            securityIdentifier = trader.securityIdentifier
        )
    }

    //===========================================================//

    @Transactional(readOnly = true)
    override suspend fun forceSellAllHolding(userId: UUID, traderId: UUID): List<TradingOrder> {
        val trader = traderRepository.getById(traderId).getOrThrow()

        val orderList = mutableListOf<TradingOrder>()

        trader.holdings.forEach { holding ->
            val order = forceSellHolding(userId, traderId, holding.id)
            orderList.add(order)
        }

        return orderList
    }

    //===========================================================//

    private suspend fun getCurrentPrice(userId: UUID, securityIdentifier: SecurityIdentifier): Quote {
        var quote = marketDataProvider.get(MarketDataProvider.Type.Finnhub).getQuote( userId, securityIdentifier)

        if(!quote.isSuccess){
            logger.warn("Finnhub quote failed for {}, trying IBKR", securityIdentifier.tickerSymbol)
            quote = marketDataProvider.get(MarketDataProvider.Type.Ibkr).getQuote( userId, securityIdentifier)
        }

        return quote.getOrThrow()
    }
}