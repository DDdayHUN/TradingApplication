package data.repository.portfolio.sql

import application.provider.HistoricalMarketDataProvider
import data.repository.security.SecurityHoldingEntity
import data.repository.security.SecurityIdentifierEntity
import data.repository.security.toDomain
import data.repository.trader.sql.TraderEntity
import domain.Portfolio
import domain.algorithm.TradingAlgorithm
import domain.market.security.SecurityHolding
import domain.trader.Trader
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.Instant
import java.util.UUID

internal class PortfolioEntityUpdateTest {
    //===========================================================//
    //===========================================================//

    private val provider = HistoricalMarketDataProvider.get(
        HistoricalMarketDataProvider.Type.YahooHistoricalMarketDataRepository
    )

    private val securityIdentifierEntity = SecurityIdentifierEntity("US0378331005", "AAPL", "USD")

    //===========================================================//
    //===========================================================//

    private fun createPortfolioEntity(
        id: UUID = UUID.randomUUID(),
        traders: MutableSet<TraderEntity> = mutableSetOf()
    ): PortfolioEntity {
        return PortfolioEntity(id = id).also { it.traders.addAll(traders) }
    }

    //===========================================================//

    private fun createTraderEntity(
        id: UUID = UUID.randomUUID(),
        capital: Double = 10_000.0,
        portfolio: PortfolioEntity,
        holdings: MutableSet<SecurityHoldingEntity> = mutableSetOf()
    ): TraderEntity {
        return TraderEntity(
            id = id,
            securityIdentifier = securityIdentifierEntity,
            portfolio = portfolio,
            capital = capital,
            algorithmType = "BUYANDHOLD",
            algorithmState = "{algorithmType: BUYANDHOLD}"
        ).also { it.holdings.addAll(holdings) }
    }

    //===========================================================//

    private fun createDomainTrader(
        id: UUID = UUID.randomUUID(),
        capital: Double = 10_000.0,
        holdings: MutableSet<SecurityHolding> = mutableSetOf()
    ): Trader {
        val securityIdentifier = securityIdentifierEntity.toDomain()

        return Trader(
            id = id,
            securityIdentifier = securityIdentifier,
            holdings = holdings,
            allocatedCapital = capital,
            algorithm = TradingAlgorithm.create(
                provider,
                TradingAlgorithm.Type.BUYANDHOLD,
                securityIdentifier
            )
        )
    }

    //===========================================================//

    private fun createDomainPortfolio(
        id: UUID = UUID.randomUUID(),
        traders: MutableSet<Trader> = mutableSetOf()
    ): Portfolio {
        return Portfolio(
            id = id,
            traders = traders
        )
    }

    //===========================================================//
    //===========================================================//

    @Test
    fun `update throws when ids do not match`() {
        val entity = createPortfolioEntity()
        val domain = createDomainPortfolio()

        assertThrows<IllegalArgumentException> {
            entity.update(domain)
        }
    }

    //===========================================================//

    @Test
    fun `update removes traders that are no longer present`() {
        val portfolioId = UUID.randomUUID()

        val traderId1 = UUID.randomUUID()
        val traderId2 = UUID.randomUUID()

        val entity = createPortfolioEntity(
            id = portfolioId
        )

        entity.traders.addAll(
            listOf(
                createTraderEntity(id = traderId1, portfolio = entity),
                createTraderEntity(id = traderId2, portfolio = entity)
            )
        )

        val domain = createDomainPortfolio(
            id = portfolioId,
            traders = mutableSetOf(
                createDomainTrader(id = traderId1)
                // traderId2 deliberately missing
            )
        )

        entity.update(domain)

        assertEquals(1, entity.traders.size)
        assertEquals(traderId1, entity.traders.first().id)
    }

    //===========================================================//

    @Test
    fun `update modifies existing traders`() {
        val portfolioId = UUID.randomUUID()
        val traderId = UUID.randomUUID()

        val entity = createPortfolioEntity(
            id = portfolioId
        )
        entity.traders.add(
            createTraderEntity(id = traderId, capital = 5_000.0, portfolio = entity)
        )

        val domain = createDomainPortfolio(
            id = portfolioId,
            traders = mutableSetOf(
                createDomainTrader(id = traderId, capital = 9_999.0)
            )
        )

        entity.update(domain)

        assertEquals(1, entity.traders.size)
        assertEquals(9_999.0, entity.traders.first().capital)
    }

    //===========================================================//

    @Test
    fun `update adds new traders`() {
        val portfolioId = UUID.randomUUID()

        val existingId = UUID.randomUUID()
        val newId = UUID.randomUUID()

        val entity = createPortfolioEntity(
            id = portfolioId
        )
        entity.traders.add(
            createTraderEntity(id = existingId, portfolio = entity)
        )

        val domain = createDomainPortfolio(
            id = portfolioId,
            traders = mutableSetOf(
                createDomainTrader(id = existingId),
                createDomainTrader(id = newId, capital = 7_500.0)
            )
        )

        entity.update(domain)

        assertEquals(2, entity.traders.size)
        assertTrue(entity.traders.any { it.id == newId })

        val newTrader = entity.traders.first { it.id == newId }
        assertEquals(entity, newTrader.portfolio)   // back-reference
        assertEquals(7_500.0, newTrader.capital)
    }

    //===========================================================//

    @Test
    fun `toDomain converts entity to domain correctly`() {
        val id = UUID.randomUUID()
        val traderId = UUID.randomUUID()
        val capital = 15_000.0

        val entity = createPortfolioEntity(id = id)
        entity.traders.add(
            createTraderEntity(id = traderId, capital = capital, portfolio = entity)
        )

        val domain = entity.toDomain()

        assertEquals(id, domain.id)
        assertEquals(1, domain.traders.size)

        val domainTrader = domain.traders.first()
        assertEquals(traderId, domainTrader.id)
        assertEquals(capital, domainTrader.availableCapital)
        assertEquals(securityIdentifierEntity.toDomain(), domainTrader.securityIdentifier)
    }

    //===========================================================//

    @Test
    fun `toEntity converts domain to entity correctly`() {
        val id = UUID.randomUUID()
        val traderId = UUID.randomUUID()
        val capital = 15_000.0
        val timestamp = Instant.now()
        val holdingId = UUID.randomUUID()

        val domain = createDomainPortfolio(
            id = id,
            traders = mutableSetOf(
                createDomainTrader(
                    id = traderId,
                    capital = capital,
                    holdings = mutableSetOf(
                        SecurityHolding(
                            id = holdingId,
                            purchasePrice = 120.0,
                            amount = 8,
                            timestamp = timestamp
                        )
                    )
                )
            )
        )

        val entity = domain.toEntity()

        assertEquals(id, entity.id)
        assertEquals(1, entity.traders.size)

        val entityTrader = entity.traders.first()
        assertEquals(traderId, entityTrader.id)
        assertEquals(capital, entityTrader.capital)
        assertEquals(entity, entityTrader.portfolio)   // back-reference
        assertEquals(1, entityTrader.holdings.size)

        val entityHolding = entityTrader.holdings.first()
        assertEquals(holdingId, entityHolding.id)
        assertEquals(120.0, entityHolding.entryPrice)
        assertEquals(8, entityHolding.amount)
        assertEquals(timestamp, entityHolding.timestamp)
        assertEquals(entityTrader, entityHolding.trader)
    }
}