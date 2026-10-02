package api.service.trader

import api.dto.ChangeTraderAlgorithmRequest
import api.dto.CreateTraderRequest
import api.service.auth.IAuthenticationService
import application.logging.logger
import application.provider.MarketDataProvider
import api.service.portfolio.IPortfolioService
import application.model.User
import data.repository.historical_data.IHistoricalMarketDataProvider
import data.repository.trader.ITraderRepository
import domain.Portfolio
import domain.algorithm.TradingAlgorithm
import domain.market.Quote
import domain.market.security.SecurityIdentifier
import domain.trader.Trader
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
    override suspend fun createTrader(request: CreateTraderRequest): Trader {
        val user = authService.currentUser()
        if(user.portfolios.isEmpty()) throw NoPortfolioExistsForUserException(user.id)

        val portfolio = user.portfolios.elementAt(0)

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
        if(user.portfolios.isEmpty()) throw NoPortfolioExistsForUserException(user.id)

        return user.portfolios.elementAt(0).traders
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
    override suspend fun deleteTrader(traderId: UUID) {
        val user = authService.currentUser()
        if(user.portfolios.isEmpty()) throw NoPortfolioExistsForUserException(user.id)

        val portfolio = user.portfolios.elementAt(0)
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
}