package application.service.spring

import api.dto.ChangeTraderAlgorithmRequest
import api.dto.CreateTraderRequest
import api.dto.TraderResponse
import application.service.IPortfolioService
import application.service.ITraderService
import domain.algorithm.TradingAlgorithm
import domain.market.security.SecurityIdentifier
import domain.trader.Trader
import domain.trader.TradingOrder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

//===========================================================//
//===========================================================//

@Service
class TraderService(
    private val portfolioService: IPortfolioService,
    private val session: AuthenticationService
) : ITraderService {
    //===========================================================//
    //===========================================================//
    // Public Method(s)

    @Transactional
    override suspend fun createTrader(portfolioId: UUID, request: CreateTraderRequest): Trader {
        val portfolio = portfolioService.getPortfolio(portfolioId)
        val securityIdentifier = SecurityIdentifier(
            isin = request.securityIdentifier.isin,
            tickerSymbol = request.securityIdentifier.tickerSymbol,
            currency = request.securityIdentifier.currency
        )
        val algorithmType = parseAlgorithmType(request.algorithmType)
        val algorithm = TradingAlgorithm.create(
            type = algorithmType,
            securityIdentifier = securityIdentifier
        )

        val trader = Trader(
            securityIdentifier = securityIdentifier,
            holdings = mutableSetOf(),
            allocatedCapital = request.capital,
            algorithm = algorithm
        )

        portfolio.changeCapital(-request.capital)
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
    suspend fun getById(userId: UUID, portfolioId: UUID, traderId: UUID): TraderResponse {
        TODO("Implement later")
    }

    //===========================================================//

    @Transactional
    suspend fun changeAlgorithm(traderId: UUID, userId: UUID,portfolioId: UUID, request: ChangeTraderAlgorithmRequest): TraderResponse {
        TODO("Implement later")
    }

    //===========================================================//

    @Transactional
    suspend fun executeTrader(userId: UUID, portfolioId: UUID, traderId: UUID): TradingOrder {
        TODO("Implement later")
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

/*
@Service
class TraderService {

    //===========================================================//
    //===========================================================//
    // Private Field(s)

    private val portfolioRepository: IPortfolioRepository

    //===========================================================//
    //===========================================================//
    // Public Method(s)

    @Transactional
    suspend fun createTrader(userId: UUID, portfolioId: UUID, request: CreateTraderRequest): TraderResponse {
        require(request.capital > 0.0) {
            "Trader capital must be greater than zero"
        }

        val portfolio = getPortfolioForUser(
            portfolioId = portfolioId,
            userId = userId
        )

        require(portfolio.capital >= request.capital){
            "Portfolio has insufficient cash amount"
        }

        val securityIdentifier = SecurityIdentifier(
            isin = request.securityIdentifier.isin,
            tickerSymbol = request.securityIdentifier.tickerSymbol,
            currency = request.securityIdentifier.currency,
        )

        val algorithmType = parseAlgorithmType(request.algorithmType)

        val algorithm = TradingAlgorithm.create(
            type = algorithmType,
            securityIdentifier = securityIdentifier,
        )

        val trader = Trader(
            securityIdentifier = securityIdentifier,
            holdings = mutableListOf(),
            allocatedCapital = request.capital,
            algorithm = algorithm,
        )

        portfolio.changeCapital(-request.capital)
        portfolio.addTrader(trader)

        portfolioRepository.save(portfolio).getOrThrow()
        return trader.toResponse()
    }

    //===========================================================//

    @Transactional(readOnly = true)
    suspend fun getAllByPortfolioId(userId: UUID, portfolioId: UUID): List<TraderResponse> {
        val portfolio = getPortfolioForUser(
            userId = userId,
            portfolioId = portfolioId
        )
        return portfolio.traders.map{ trader ->
            trader.toResponse()
        }
    }

    //===========================================================//

    @Transactional(readOnly = true)
    suspend fun getById(userId: UUID, portfolioId: UUID, traderId: UUID): TraderResponse {
        val portfolio = getPortfolioForUser(
            userId = userId,
            portfolioId = portfolioId
        )
        val trader = portfolio.traders.find { trader ->
            trader.id == traderId
        }?: throw TraderNotFoundException(traderId,userId)

        return trader.toResponse()
    }

    //===========================================================//

    @Transactional
    suspend fun changeAlgorithm(traderId: UUID, userId: UUID,portfolioId: UUID, request: ChangeTraderAlgorithmRequest): TraderResponse {
        val portfolio = getPortfolioForUser(
            userId = userId,
            portfolioId = portfolioId
        )

        val trader = portfolio.traders.find{ trader ->
            trader.id == traderId
        }?: throw TraderNotFoundException(traderId,userId)

        val algorithmType = parseAlgorithmType(request.algorithmType)

        val algorithm = TradingAlgorithm.create(
            type = algorithmType,
            securityIdentifier = trader.securityIdentifier
        )

        trader.changeAlgorithm(algorithm)

        portfolioRepository.save(portfolio).getOrThrow()

        return trader.toResponse()
    }

    //===========================================================//

    @Transactional
    suspend fun executeTrader(userId: UUID, portfolioId: UUID, traderId: UUID): TradingOrder {
        val portfolio = getPortfolioForUser(
            portfolioId = portfolioId,
            userId = userId
        )

        val trader = portfolio.traders.find {trader ->
            trader.id == traderId
        } ?: throw TraderNotFoundException(traderId,userId)

        val quote = MarketDataProvider.create(MarketDataProvider.Type.Finnhub)
            .getQuote(trader.securityIdentifier).getOrThrow()

        val order = trader.createOrder(quote)

        trader.finalizeOrder(order)

        portfolioRepository.save(portfolio).getOrThrow()

        return order
    }

    //===========================================================//
    //===========================================================//
    // Helper Method(s)

    private suspend fun getPortfolioForUser(portfolioId: UUID, userId: UUID): Portfolio {
        val portfolio = portfolioRepository.getById(portfolioId).getOrThrow()
        if(portfolio.userId != userId ) throw PortfolioNotFoundException(portfolioId)
        return portfolio
    }

    //===========================================================//

    private fun parseAlgorithmType(value: String): TradingAlgorithm.Type {
        return when (value.trim().uppercase()) {
            "TACPP46" -> TradingAlgorithm.Type.TACPP46
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
    //===========================================================//
    // Constructor(s)

    constructor(portfolioRepository: IPortfolioRepository) {
        this.portfolioRepository = portfolioRepository
    }

}
 */