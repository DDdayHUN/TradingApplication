package application.service.trader

import api.dto.ChangeTraderAlgorithmRequest
import api.dto.CreateTraderRequest
import application.logging.logger
import application.provider.MarketDataProvider
import application.service.portfolio.IPortfolioService
import application.service.user.IUserService
import data.repository.historical_data.IHistoricalMarketDataProvider
import data.repository.trader.ITraderRepository
import data.repository.trader.sql.toDomain
import data.repository.trader.sql.update
import domain.algorithm.ITradingAlgorithm
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
    private val historicalMarketDataProvider: IHistoricalMarketDataProvider,
    private val portfolioService: IPortfolioService,
    private val userService: IUserService,
    private val marketDataProvider: MarketDataProvider,
    private val traderRepository: ITraderRepository
) : ITraderService {

    //===========================================================//
    //===========================================================//
    // Private Field(s)

    private val m_Logger = logger<TraderService>()

    //===========================================================//
    //===========================================================//
    // Public Method(s)

    @Transactional
    @Deprecated("This will be superseded by another function")
    override suspend fun executeTrader(userId: UUID, traderId: UUID): TradingOrder {
        val traderEntity = traderRepository.getById(traderId).getOrThrow()
        val traderDomain = traderEntity.toDomain()

        val quote = getCurrentPrice(userId, traderDomain.securityIdentifier)
        val order = traderDomain.createOrder(quote)

        traderEntity.update(traderDomain)
        traderRepository.save(traderEntity).getOrThrow()

        return order
    }

    //===========================================================//

    @Transactional
    override suspend fun applyBuyFill(traderId: UUID, filledQuantity: Int, averageFillPrice: Double) {
        val traderEntity = traderRepository.getById(traderId).getOrThrow()
        val traderDomain = traderEntity.toDomain()

        traderDomain.applyBuyFill(
            price = averageFillPrice,
            amount = filledQuantity
        )

        traderEntity.update(traderDomain)
        traderRepository.save(traderEntity).getOrThrow()
    }

    //===========================================================//

    @Transactional
    override suspend fun applySellFill(traderId: UUID, sellAllocations: Set<SellHolding>, averageFillPrice: Double) {
        val traderEntity = traderRepository.getById(traderId).getOrThrow()
        val traderDomain = traderEntity.toDomain()

        traderDomain.applySellFill(
            price = averageFillPrice,
            holdingsToSell = sellAllocations
        )

        traderEntity.update(traderDomain)
        traderRepository.save(traderEntity).getOrThrow()
    }

    //===========================================================//

    @Transactional(readOnly = true)
    override suspend fun forceSellHolding(userId: UUID, traderId: UUID, securityHoldingId: UUID): TradingOrder {
       val trader = traderRepository.getById(traderId).getOrThrow().toDomain()

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
        val trader = traderRepository.getById(traderId).getOrThrow().toDomain()

        val orderList = mutableListOf<TradingOrder>()

        trader.holdings.forEach { holding ->
            val order = forceSellHolding(userId, traderId, holding.id)
            orderList.add(order)
        }

        return orderList
    }

    //===========================================================//

    @Transactional
    override suspend fun createTrader(userId: UUID, request: CreateTraderRequest): Trader {
        val user = userService.getById(userId)

        val portfolio = user.portfolio

        val securityIdentifier = SecurityIdentifier(
            isin = request.securityIdentifier.isin,
            tickerSymbol = request.securityIdentifier.tickerSymbol,
            currency = request.securityIdentifier.currency
        )

        val algorithmType = ITradingAlgorithm.typeFromTag(request.algorithmType).getOrThrow()
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
        portfolioService.update(user.id, portfolio)

        return trader
    }

    //===========================================================//

    @Transactional(readOnly = true)
    override suspend fun getAll(userId: UUID): Set<Trader> {
        val user = userService.getById(userId)

        return user.portfolio.traders
    }

    //===========================================================//

    @Transactional(readOnly = true)
    override suspend fun getById(userId: UUID, traderId: UUID): Trader {
        val user = userService.getById(userId)
        return user.portfolio.traders.find { trader -> trader.id == traderId}
            ?: throw TraderNotFoundException(traderId)
    }

    //===========================================================//

    @Transactional
    override suspend fun changeAlgorithm(traderId: UUID, request: ChangeTraderAlgorithmRequest): Trader {
        val traderEntity = traderRepository.getById(traderId).getOrThrow()
        val traderDomain = traderEntity.toDomain()

        val algorithm = TradingAlgorithm.create(
            provider = historicalMarketDataProvider,
            type = ITradingAlgorithm.typeFromTag(request.algorithmType).getOrThrow(),
            securityIdentifier = traderDomain.securityIdentifier,
        )

        traderDomain.changeAlgorithm(algorithm)
        traderEntity.update(traderDomain)

        return traderRepository.save(traderEntity).getOrThrow().toDomain()
    }

    //===========================================================//

    @Transactional
    override suspend fun deleteTrader(userId: UUID, traderId: UUID) {
        val user = userService.getById(userId)
        val portfolio = user.portfolio
        val trader = getById(user.id, traderId)

        portfolio.removeTrader(trader)
        portfolioService.update(user.id, portfolio)
    }

    //===========================================================//

    private suspend fun getCurrentPrice(userId: UUID, securityIdentifier: SecurityIdentifier): Quote {
        var quote = marketDataProvider.get(MarketDataProvider.Type.Finnhub).getQuote( userId, securityIdentifier)

        if(!quote.isSuccess){
            m_Logger.warn("Finnhub quote failed for {}, trying IBKR", securityIdentifier.tickerSymbol)
            quote = marketDataProvider.get(MarketDataProvider.Type.Ibkr).getQuote( userId, securityIdentifier)
        }

        return quote.getOrThrow()
    }
}