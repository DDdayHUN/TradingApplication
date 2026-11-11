package application.service.trader

import application.logging.logger
import application.provider.MarketDataProvider
import data.repository.g_GSON
import data.repository.historical_data.IHistoricalMarketDataProvider
import data.repository.portfolio.IPortfolioRepository
import data.repository.security.toEntity
import data.repository.trader.ITraderRepository
import data.repository.trader.sql.TraderEntity
import data.repository.trader.sql.toDomain
import data.repository.trader.sql.toEntity
import domain.algorithm.TradingAlgorithm
import domain.market.Quote
import domain.market.security.SecurityIdentifier
import domain.trader.SellSecurityHolding
import domain.trader.Trader
import domain.trader.TradingOrder
import exception.TraderNotFoundException
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
class TraderService(
    @param:Qualifier("yahoo")
    private val historicalMarketDataProvider: IHistoricalMarketDataProvider,
    private val currentMarketDataProvider: MarketDataProvider,
    private val portfolioRepository: IPortfolioRepository,
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
    override suspend fun createTrader(portfolioId: UUID, trader: Trader): TraderEntity {
        val portfolioEntity = portfolioRepository.getById(portfolioId).getOrThrow()

        traderRepository.getById(trader.id)
            .onSuccess {
                m_Logger.warn("Trader with ID: {${trader.id}} already exists.")
                throw IllegalArgumentException("Trader with ID: {${trader.id}} already exists.")
            }

        val traderEntity = traderRepository.save(trader.toEntity(portfolioEntity)).getOrThrow()
        portfolioEntity.traders.add(traderEntity)

        m_Logger.info("Created trader with ID:{${trader.id}} for portfolio with ID:{$portfolioId}")

        return traderEntity
    }

    //===========================================================//

    @Transactional
    override suspend fun updateTrader(trader: Trader): TraderEntity {
        val traderEntity = traderRepository.getById(trader.id).getOrThrow()
        traderEntity.securityIdentifier = trader.securityIdentifier.toEntity()
        traderEntity.capital = trader.availableCapital
        traderEntity.algorithmType = TradingAlgorithm.typeTagOf(trader.algorithm).getOrThrow()
        traderEntity.algorithmState = g_GSON.toJson(
            trader.algorithm,
            TradingAlgorithm::class.java
        )

        val domainHoldingsById = trader.holdings.associateBy { it.id }
        val entityHoldingsById = traderEntity.holdings.associateBy { it.id }

        // remove holdings that no longer exist
        traderEntity.holdings.removeIf { it.id !in domainHoldingsById }

        // add new holdings
        trader.holdings
            .filter { it.id !in entityHoldingsById }
            .forEach {
                traderEntity.holdings.add(it.toEntity(traderEntity))
            }

        // update existing holdings
        traderEntity.holdings
            .forEach { holdingEntity ->
                domainHoldingsById[holdingEntity.id]?.let { domainHolding ->
                    holdingEntity.amount = domainHolding.amount
                }
            }


        return traderRepository.save(traderEntity).getOrThrow()
    }

    //===========================================================//

    @Transactional
    override suspend fun deleteTrader(traderId: UUID) {
        val traderEntity = traderRepository.getById(traderId).getOrThrow()

        if(traderEntity.holdings.isNotEmpty()) throw IllegalStateException("Holdings are not empty for trader with ID: {$traderId}")
        val portfolioEntity = traderEntity.portfolio
        portfolioEntity.traders.remove(traderEntity)

        m_Logger.info("Deleted trader with ID:{$traderId}")
    }

    //===========================================================//

    @Transactional(readOnly = true)
    override suspend fun getTraderById(userId: UUID, traderId: UUID): Trader {
        val user = userService.getById(userId)
        return user.portfolio.traders.find { trader -> trader.id == traderId }
            ?: throw TraderNotFoundException(traderId)
    }

    //===========================================================//

    @Transactional(readOnly = true)
    override suspend fun getAllTrader(userId: UUID): List<Trader> {
        val user = userService.getById(userId)
        return user.portfolio.traders.toList()
    }

    //===========================================================//

    @Transactional
    override suspend fun executeAllTrader(userId: UUID): List<TradingOrder> {
        val user = userService.getById(userId)
        val traders = user.portfolio.traders

        val orders = traders.flatMap { trader ->
            trader.runAlgorithm(getCurrentQuote(user.id, trader.securityIdentifier))
        }

        userService.update(user)

        m_Logger.info("All traders were executed for user with ID:{$userId}")

        return orders
    }

    //===========================================================//

    @Transactional
    override suspend fun executeAllTrader(): List<TradingOrder> {
        val userIds = userService.getAllUserId()
        val orders = userIds.flatMap { userId -> executeAllTrader(userId) }
        return orders
    }

    //===========================================================//

    @Transactional(readOnly = true)
    override suspend fun forceSellAllHolding(userId: UUID, traderId: UUID): TradingOrder {
        val trader = traderRepository.getById(traderId).getOrThrow().toDomain()

        val holdingsToBeSold = trader.holdings
            .map { holding -> SellSecurityHolding(holding, holding.amount) }
            .toSet()

        val order = TradingOrder(
            traderId = trader.id,
            securityIdentifier = trader.securityIdentifier,
            signal = TradingOrder.Signal.Sell(holdingsToBeSold),
            atPrice = getCurrentQuote(userId, trader.securityIdentifier).currentPrice
        )

        m_Logger.info("Force Sell of all holdings for user with ID:{$userId} was applied to trader with ID:{$traderId}")

        return order
    }

    //===========================================================//

    @Transactional(readOnly = true)
    override suspend fun forceSellAllHolding(userId: UUID): List<TradingOrder> {
        val user = userService.getById(userId)

        val orders = user.portfolio.traders.map { trader ->
            val holdingsToBeSold = trader.holdings
                .map { holding -> SellSecurityHolding(holding, holding.amount) }
                .toSet()

            TradingOrder(
                traderId = trader.id,
                securityIdentifier = trader.securityIdentifier,
                signal = TradingOrder.Signal.Sell(holdingsToBeSold),
                atPrice = getCurrentQuote(userId, trader.securityIdentifier).currentPrice
            )
        }

        m_Logger.info("Force Sell of all holdings of all traders for user with ID:{$userId}")

        return orders
    }

    //===========================================================//

    @Transactional
    override suspend fun changeAlgorithm(traderId: UUID, algorithmType: TradingAlgorithm.Type): Trader {
        val traderEntity = traderRepository.getById(traderId).getOrThrow()
        val traderDomain = traderEntity.toDomain()

        val algorithm = TradingAlgorithm.create(
            type = algorithmType,
            history = historicalMarketDataProvider.getBySecurityIdentifier(traderDomain.securityIdentifier).getOrThrow()
        ).getOrThrow()

        traderDomain.changeAlgorithm(algorithm)
        traderEntity.update(traderDomain)

        return traderEntity.toDomain()
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

        m_Logger.info("Buy Fill was applied for trader with ID:{$traderId}")
    }

    //===========================================================//

    @Transactional
    override suspend fun applySellFill(traderId: UUID, sellAllocations: Set<SellSecurityHolding>, averageFillPrice: Double) {
        val traderEntity = traderRepository.getById(traderId).getOrThrow()
        val traderDomain = traderEntity.toDomain()

        traderDomain.applySellFill(
            price = averageFillPrice,
            holdingsToSell = sellAllocations
        )

        traderEntity.update(traderDomain)

        m_Logger.info("Sell Fill was applied for trader with ID:{$traderId}")
    }

    //===========================================================//

    private suspend fun getCurrentQuote(userId: UUID, securityIdentifier: SecurityIdentifier): Quote {
        var quote = currentMarketDataProvider.get(MarketDataProvider.Type.Finnhub).getQuote(userId, securityIdentifier)

        if(!quote.isSuccess) {
            m_Logger.warn("Finnhub quote failed for {}, trying IBKR", securityIdentifier.tickerSymbol)
            quote = currentMarketDataProvider.get(MarketDataProvider.Type.Ibkr).getQuote(userId, securityIdentifier)
        }

        return quote.getOrThrow()
    }
}