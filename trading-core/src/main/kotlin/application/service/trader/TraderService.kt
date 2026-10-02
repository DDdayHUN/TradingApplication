package application.service.trader

import api.dto.ChangeTraderAlgorithmRequest
import api.dto.CreateTraderRequest
import api.service.auth.IAuthenticationService
import api.service.portfolio.IPortfolioService
import application.logging.logger
import application.model.User
import application.provider.MarketDataProvider
import data.repository.historical_data.IHistoricalMarketDataProvider
import data.repository.trader.ITraderRepository
import domain.algorithm.TradingAlgorithm
import domain.market.Quote
import domain.market.security.SecurityIdentifier
import domain.trader.SellHolding
import domain.trader.Trader
import domain.trader.TradingOrder
import exception.api.HoldingNotFoundException
import exception.api.NoPortfolioExistsForUserException
import exception.api.TraderNotFoundException
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
class TraderService(
    @param:Qualifier("yahoo")
    private val historicalMarketDataProvider: IHistoricalMarketDataProvider,
    private val portfolioService: IPortfolioService,
    private val authService: IAuthenticationService,
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

    @Transactional
    override suspend fun createTrader(user: User, request: CreateTraderRequest): Trader {

        val portfolio = user.portfolio

        val securityIdentifier = SecurityIdentifier(
            isin = request.securityIdentifier.isin,
            tickerSymbol = request.securityIdentifier.tickerSymbol,
            currency = request.securityIdentifier.currency
        )

        val algorithmType = parseAlgorithmType(request.algorithmType)
        val algorithm = TradingAlgorithm.create(
            provider = historicalMarketDataProvider,
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
        portfolioService.update(portfolio)

        return trader
    }

    //===========================================================//

    @Transactional(readOnly = true)
    override suspend fun getAll(): Set<Trader> {
        val user = authService.currentUser()
        if(user.portfolio == null) throw NoPortfolioExistsForUserException(user.id)

        return user.portfolio.traders
    }

    //===========================================================//

    @Transactional(readOnly = true)
    override suspend fun getById(traderId: UUID): Trader {
        return try {
            getAll().first { it.id == traderId }
        }
        catch (ex: NoSuchElementException) {
            throw TraderNotFoundException(traderId)
        }
    }

    //===========================================================//

    @Transactional
    override suspend fun changeAlgorithm(traderId: UUID, request: ChangeTraderAlgorithmRequest): Trader {
        val trader = getById(traderId)

        val algorithm = TradingAlgorithm.create(
            provider = historicalMarketDataProvider,
            type = parseAlgorithmType(request.algorithmType),
            securityIdentifier = trader.securityIdentifier,
        )

        trader.changeAlgorithm(algorithm)
        traderRepository.save(trader)

        return trader
    }

    //===========================================================//

    @Transactional
    override suspend fun deleteTrader(user: User, traderId: UUID) {
        val portfolio = user.portfolio
        val trader = getById(traderId)

        portfolio.removeTrader(trader)
        portfolioService.update(portfolio)
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