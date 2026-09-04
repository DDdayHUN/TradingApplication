package application.service.trader

import api.dto.ChangeTraderAlgorithmRequest
import api.dto.CreateTraderRequest
import application.logging.logger
import application.service.portfolio.IPortfolioService
import application.provider.MarketDataProvider
import data.network.finnhub.FinnhubConfig
import domain.algorithm.TradingAlgorithm
import data.repository.historical_data.IHistoricalMarketDataProvider
import domain.market.security.SecurityIdentifier
import domain.trader.Trader
import domain.trader.TradingOrder
import exception.api.TraderNotFoundException
import infrastructure.broker.IbkrSession
import infrastructure.broker.SellAllocation
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class TraderService(
    @param:Qualifier("yahoo")
    private val provider: IHistoricalMarketDataProvider,
    private val portfolioService: IPortfolioService,
    private val ibkrSession: IbkrSession,
    private val finnhubConfig: FinnhubConfig,
) : ITraderService {

    //===========================================================//
    //===========================================================//
    // Private Field(s)

    private val logger = logger<TraderService>()

    //===========================================================//
    //===========================================================//
    // Public Method(s)

    @Transactional
    override suspend fun createTrader(userId: UUID, portfolioId: UUID, request: CreateTraderRequest): Trader {
        val portfolio = portfolioService.getPortfolio(userId, portfolioId)
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
    override suspend fun getById(portfolioId: UUID, traderId: UUID): Trader? {
        val portfolio =  portfolioService.getPortfolio(portfolioId)

        val trader = portfolio.traders.find { trader ->
            trader.id == traderId
        }

        return trader
    }

    //===========================================================//

    @Transactional
    override suspend fun changeAlgorithm(portfolioId: UUID, traderId: UUID, request: ChangeTraderAlgorithmRequest): Trader {
        val portfolio = portfolioService.getPortfolio(portfolioId)
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
    override suspend fun executeTrader(portfolioId: UUID, traderId: UUID): TradingOrder {
        val portfolio = portfolioService.getPortfolio(portfolioId)

        val trader = portfolio.traders.find {trader ->
            trader.id == traderId
        }?: throw TraderNotFoundException(traderId)

        val finnhubProvider = MarketDataProvider.create(MarketDataProvider.Type.Finnhub(finnhubConfig))
        var quote = finnhubProvider.getQuote(trader.securityIdentifier)

        if(!quote.isSuccess){
            logger.warn("Finnhub quote failed for {}, trying IBKR", trader.securityIdentifier.tickerSymbol)
            quote = MarketDataProvider.create(MarketDataProvider.Type.Ibkr(ibkrSession)).getQuote(trader.securityIdentifier)
        }

       // val quote = Quote(160.0)
        val order = trader.createOrder(quote.getOrThrow())

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
}