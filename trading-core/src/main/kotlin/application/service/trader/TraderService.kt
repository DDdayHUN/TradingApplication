package application.service.trader

import api.dto.ChangeTraderAlgorithmRequest
import api.dto.CreateTraderRequest
import application.logging.logger
import application.provider.MarketDataProvider
import application.service.auth.IAuthenticationService
import application.service.portfolio.IPortfolioService
import data.repository.historical_data.IHistoricalMarketDataProvider
import data.repository.trader.ITraderRepository
import domain.algorithm.TradingAlgorithm
import domain.market.Quote
import domain.market.security.SecurityIdentifier
import domain.trader.SellHolding
import domain.trader.Trader
import domain.trader.TradingOrder
import exception.api.HoldingNotFoundException
import exception.api.TraderNotFoundException
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
class TraderService(
    @param:Qualifier("yahoo")
    private val historicalProvider: IHistoricalMarketDataProvider,
    private val marketDataProvider: MarketDataProvider,
    private val portfolioService: IPortfolioService,
    private val traderRepository: ITraderRepository
) : ITraderService {

    //===========================================================//
    //===========================================================//
    // Private Field(s)

    private val logger = logger<TraderService>()

    //===========================================================//
    //===========================================================//
    // Public Method(s)

    @Transactional
    override suspend fun createTrader(userId: UUID, request: CreateTraderRequest): Trader {
        val portfolio = portfolioService.getPortfolioByUserId(userId)
        val availableCapital = portfolioService.getPortfolioAccountSummary(userId).availableCapital

        require(request.capital <= availableCapital){
            "Insufficient available capital to create new trader"
        }

        val securityIdentifier = SecurityIdentifier(
            isin = request.securityIdentifier.isin,
            tickerSymbol = request.securityIdentifier.tickerSymbol,
            currency = request.securityIdentifier.currency
        )
        val algorithmType = parseAlgorithmType(request.algorithmType)
        val algorithm = TradingAlgorithm.create(
            provider = historicalProvider,
            type = algorithmType,
            securityIdentifier = securityIdentifier
        )

        val trader = Trader(
            securityIdentifier = securityIdentifier,
            holdings = mutableSetOf(),
            allocatedCapital = request.capital,
            algorithm = algorithm
        )

        portfolio.addTrader(trader)

        portfolioService.save(portfolio)

        return trader
    }

    //===========================================================//

    @Transactional(readOnly = true)
    override suspend fun getAll(userId: UUID): Set<Trader> {
       return portfolioService.getPortfolioByUserId(userId).traders
    }

    //===========================================================//

    @Transactional(readOnly = true)
    override suspend fun getById(traderId: UUID): Trader {
        val trader = traderRepository.getById(traderId)
        return trader.getOrThrow()
    }

    //===========================================================//

    @Transactional
    override suspend fun changeAlgorithm(traderId: UUID, request: ChangeTraderAlgorithmRequest): Trader {
        val trader = traderRepository.getById(traderId).getOrThrow()

        val algorithmType = parseAlgorithmType(request.algorithmType)

        val algorithm = TradingAlgorithm.create(
            provider = historicalProvider,
            type = algorithmType,
            securityIdentifier = trader.securityIdentifier,
        )

        trader.changeAlgorithm(algorithm)

        traderRepository.save(trader)

        return trader
    }

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

    @Transactional
    override suspend fun deleteTrader(userId: UUID, traderId: UUID) {
        val portfolio = portfolioService.getPortfolioByUserId(userId)

        val trader = portfolio.traders.find {trader ->
            trader.id == traderId
        } ?: throw TraderNotFoundException(traderId)

        portfolio.removeTrader(trader)

        portfolioService.save(portfolio)
    }


    //===========================================================//

    private fun parseAlgorithmType(value: String): TradingAlgorithm.Type {
        return when (value.trim().uppercase()) {
            "TACPP46" -> TradingAlgorithm.Type.TACPP46
            "TACPP462" -> TradingAlgorithm.Type.TACPP462
            "ALGDES2" -> TradingAlgorithm.Type.ALGDES2
            "ALGDES3" -> TradingAlgorithm.Type.ALGDES3
            "ALGDES31" -> TradingAlgorithm.Type.ALGDES31
            "ALGDES4" -> TradingAlgorithm.Type.ALGDES4
            "BUYANDHOLD" -> TradingAlgorithm.Type.BUYANDHOLD

            else -> throw IllegalArgumentException(
                "Unsupported algorithm type: $value"
            )
        }
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