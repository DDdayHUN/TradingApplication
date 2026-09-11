package application.service.trader

import api.dto.ChangeTraderAlgorithmRequest
import api.dto.CreateTraderRequest
import application.logging.logger
import application.provider.MarketDataProvider
import application.service.auth.IAuthenticationService
import application.service.portfolio.IPortfolioService
import data.network.finnhub.FinnhubConfig
import data.repository.historical_data.IHistoricalMarketDataProvider
import domain.algorithm.TradingAlgorithm
import domain.market.Quote
import domain.market.security.SecurityIdentifier
import domain.order.Order
import domain.trader.Trader
import exception.api.HoldingNotFoundException
import exception.api.TraderNotFoundException
import infrastructure.broker.IbkrSession
import infrastructure.broker.SellAllocation
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
class TraderService(
    @param:Qualifier("yahoo")
    private val provider: IHistoricalMarketDataProvider,
    private val portfolioService: IPortfolioService,
    private val ibkrSession: IbkrSession,
    private val finnhubConfig: FinnhubConfig
) : ITraderService {

    //===========================================================//
    //===========================================================//
    // Private Field(s)

    private val logger = logger<TraderService>()

    //===========================================================//
    //===========================================================//
    // Public Method(s)

    @Transactional
    override suspend fun createTrader(portfolioId: UUID, request: CreateTraderRequest): Trader {
        val portfolio = portfolioService.getPortfolio(portfolioId)
        val availableCapital = portfolioService.getAccountSummary(portfolioId).availableCapital

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
            provider = provider,
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
    override suspend fun getAllByPortfolioId(portfolioId: UUID): Set<Trader> {
       return portfolioService.getPortfolio(portfolioId).traders
    }

    //===========================================================//

    @Transactional(readOnly = true)
    override suspend fun getById(traderId: UUID): Trader? {
        val portfolio =  portfolioService.getPortfolioByTraderId(traderId)

        val trader = portfolio.traders.find { trader ->
            trader.id == traderId
        }

        return trader
    }

    //===========================================================//

    @Transactional
    override suspend fun changeAlgorithm(traderId: UUID, request: ChangeTraderAlgorithmRequest): Trader {
        val portfolio = portfolioService.getPortfolioByTraderId(traderId)
        val trader = portfolio.traders.find {trader ->
            trader.id == traderId
        } ?: throw TraderNotFoundException(traderId)

        val algorithmType = parseAlgorithmType(request.algorithmType)

        val algorithm = TradingAlgorithm.create(
            provider = provider,
            type = algorithmType,
            securityIdentifier = trader.securityIdentifier,
        )

        trader.changeAlgorithm(algorithm)

        portfolioService.save(portfolio)

        return trader
    }

    //===========================================================//

    @Transactional
    override suspend fun executeTrader(traderId: UUID): Order? {
        val portfolio = portfolioService.getPortfolioByTraderId(traderId)

        val trader = portfolio.traders.find {trader ->
            trader.id == traderId
        }?: throw TraderNotFoundException(traderId)

        val quote = getCurrentPrice(trader.securityIdentifier)
       // val quote = Quote(160.0)
        val order = trader.createOrder(quote) ?: return null

        portfolioService.save(portfolio)

        return order
    }

    //===========================================================//

    @Transactional
    override suspend fun applyBuyFill(traderId: UUID, filledQuantity: Int, averageFillPrice: Double) {
        val portfolio = portfolioService.getPortfolioByTraderId(traderId)

        val trader = portfolio.traders.find {
            it.id == traderId
        } ?: throw TraderNotFoundException(traderId)

        trader.applyBuyFill(
            price = averageFillPrice,
            amount = filledQuantity
        )

        portfolioService.save(portfolio)
    }

    //===========================================================//

    @Transactional
    override suspend fun applySellFill(
        traderId: UUID,
        sellAllocations: List<SellAllocation>,
        averageFillPrice: Double
    ) {
        val portfolio = portfolioService.getPortfolioByTraderId(traderId)

        val trader = portfolio.traders.find {trader ->
            trader.id == traderId
        }?: throw TraderNotFoundException(traderId)

        trader.applySellFill(
            price = averageFillPrice,
            allocations = sellAllocations
        )

        portfolioService.save(portfolio)
    }

    @Transactional(readOnly = true)
    override suspend fun forceSellHolding(traderId: UUID, securityHoldingId: UUID): Order {
        val portfolio = portfolioService.getPortfolioByTraderId(traderId)
        val trader = portfolio.traders.find { trader ->
            trader.id == traderId
        }?: throw TraderNotFoundException(traderId)

        val holding = trader.holdings.find {holding ->
            holding.id == securityHoldingId
        }?: throw HoldingNotFoundException(securityHoldingId)

        val order = Order(
            traderId = trader.id,
            securityIdentifier = trader.securityIdentifier,
            signal = Order.Signal.Sell(
                allocations = mutableListOf(
                    SellAllocation(
                        holdingId = holding.id,
                        amount = holding.amount
                ))
            ),
            signalPrice = getCurrentPrice(trader.securityIdentifier).currentPrice,
        )
        return order
    }

    @Transactional(readOnly = true)
    override suspend fun forceSellAllHolding(traderId: UUID): List<Order> {
        val portfolio = portfolioService.getPortfolioByTraderId(traderId)
        val trader = portfolio.traders.find { trader ->
            trader.id == traderId
        } ?: throw TraderNotFoundException(traderId)

        val orderList = mutableListOf<Order>()

        trader.holdings.forEach { holding ->
            val order = forceSellHolding(traderId, holding.id)
            orderList.add(order)
        }

        return orderList
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

    private suspend fun getCurrentPrice(securityIdentifier: SecurityIdentifier): Quote{
        val finnhubProvider = MarketDataProvider.create(MarketDataProvider.Type.Finnhub(finnhubConfig))
        var quote = finnhubProvider.getQuote(securityIdentifier)

        if(!quote.isSuccess){
            logger.warn("Finnhub quote failed for {}, trying IBKR", securityIdentifier.tickerSymbol)
            quote = MarketDataProvider.create(MarketDataProvider.Type.Ibkr(ibkrSession)).getQuote(securityIdentifier)
        }

        return quote.getOrThrow()
    }

}