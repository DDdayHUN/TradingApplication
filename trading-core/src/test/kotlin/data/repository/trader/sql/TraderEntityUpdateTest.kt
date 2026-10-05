package data.repository.trader.sql

import application.provider.HistoricalMarketDataProvider
import data.repository.portfolio.sql.PortfolioEntity
import data.repository.security.SecurityHoldingEntity
import data.repository.security.SecurityIdentifierEntity
import data.repository.security.toDomain
import domain.algorithm.TradingAlgorithm
import domain.market.security.SecurityHolding
import domain.trader.Trader
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.Instant
import java.util.UUID

class TraderEntityUpdateTest {
    //===========================================================//
    //===========================================================//

    private val provider = HistoricalMarketDataProvider.get(
        HistoricalMarketDataProvider.Type.YahooHistoricalMarketDataRepository
    )

    private val traderId = UUID.randomUUID()
    private val portfolio = PortfolioEntity(id = UUID.randomUUID())
    private val securityIdentifierEntity = SecurityIdentifierEntity("US0378331005", "AAPL", "USD")

    //===========================================================//
    //===========================================================//

    private fun createTraderEntity(id: UUID = traderId, capital: Double = 10_000.0, holdings: MutableSet<SecurityHoldingEntity> = mutableSetOf()): TraderEntity {
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

    private fun createDomainTrader(id: UUID = traderId, capital: Double = 10_000.0, holdings: MutableSet<SecurityHolding> = mutableSetOf()): Trader {
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
    //===========================================================//

    @Test
    fun `update throws when ids do not match`() {
        val entity = createTraderEntity()
        val domain = createDomainTrader(id = UUID.randomUUID())

        assertThrows<IllegalArgumentException> {
            entity.update(domain)
        }
    }

    //===========================================================//

    @Test
    fun `update copies scalar fields correctly`() {
        val entity = createTraderEntity(capital = 5_000.0)
        val domain = createDomainTrader(capital = 12_500.0)

        entity.update(domain)

        assertEquals(12_500.0, entity.capital)
        assertEquals(entity.id, domain.id)
    }

    //===========================================================//

    @Test
    fun `update removes holdings that are no longer present`() {
        val holdingId1 = UUID.randomUUID()
        val holdingId2 = UUID.randomUUID()
        val timestamp1 = Instant.now()
        val timestamp2 = Instant.now()

        val entity = createTraderEntity()

        entity.holdings.addAll(
            listOf(
                SecurityHoldingEntity(id = holdingId1, entryPrice = 100.0, amount = 10, timestamp = timestamp1, trader = entity),
                SecurityHoldingEntity(id = holdingId2, entryPrice = 200.0, amount = 5, timestamp = timestamp2, trader = entity)
            )
        )

        val domain = createDomainTrader(
            holdings = mutableSetOf(
                SecurityHolding(
                    id = holdingId1,
                    purchasePrice = 100.0,
                    amount = 10,
                    timestamp = timestamp1
                )
                // holdingId2 deliberately missing
            )
        )

        entity.update(domain)

        assertEquals(1, entity.holdings.size)
        assertEquals(holdingId1, entity.holdings.first().id)
    }

    //===========================================================//

    @Test
    fun `update modifies existing holdings`() {
        val holdingId = UUID.randomUUID()
        val oldTimestamp = Instant.now().minusSeconds(3600)
        val newTimestamp = Instant.now()

        val entity = createTraderEntity()
        entity.holdings.add(
            SecurityHoldingEntity(id = holdingId, entryPrice = 100.0, amount= 10, timestamp = oldTimestamp, trader = entity)
        )

        val domain = createDomainTrader(
            holdings = mutableSetOf(
                SecurityHolding(
                    id = holdingId,
                    purchasePrice = 150.0,
                    amount = 20,
                    timestamp = newTimestamp
                )
            )
        )

        entity.update(domain)

        val updated = entity.holdings.first()
        assertEquals(150.0, updated.entryPrice)
        assertEquals(20, updated.amount)
        assertEquals(newTimestamp, updated.timestamp)
    }

    //===========================================================//

    @Test
    fun `update adds new holdings`() {
        val existingId = UUID.randomUUID()
        val newId = UUID.randomUUID()
        val timestamp = Instant.now()

        val entity = createTraderEntity()
        entity.holdings.add(
            SecurityHoldingEntity(id= existingId, entryPrice = 100.0, amount = 10, timestamp = timestamp, trader = entity)
        )

        val domain = createDomainTrader(
            holdings = mutableSetOf(
                SecurityHolding(
                    id = existingId,
                    purchasePrice = 100.0,
                    amount = 10,
                    timestamp = timestamp
                ),
                SecurityHolding(
                    id = newId,
                    purchasePrice = 200.0,
                    amount = 5,
                    timestamp = timestamp
                )
            )
        )

        entity.update(domain)

        assertEquals(2, entity.holdings.size)
        assertTrue(entity.holdings.any { it.id == newId })

        // back-reference should be set
        val newHolding = entity.holdings.first { it.id == newId }
        assertEquals(entity, newHolding.trader)
    }

    //===========================================================//

    @Test
    fun `toDomain converts entity to domain correctly`() {
        val id = UUID.randomUUID()
        val capital = 15_000.0
        val timestamp = Instant.now()
        val holdingId = UUID.randomUUID()

        val entity = createTraderEntity(id = id, capital = capital, mutableSetOf())
        entity.holdings.add(
            SecurityHoldingEntity(
                id = holdingId,
                entryPrice = 120.0,
                amount = 8,
                timestamp = timestamp,
                trader = entity
            )
        )

        val domain = entity.toDomain()

        assertEquals(id, domain.id)
        assertEquals(capital, domain.availableCapital)
        assertEquals(securityIdentifierEntity.toDomain(), domain.securityIdentifier)
        assertEquals(1, domain.holdings.size)

        val domainHolding = domain.holdings.first()
        assertEquals(holdingId, domainHolding.id)
        assertEquals(120.0, domainHolding.purchasePrice)
        assertEquals(8, domainHolding.amount)
        assertEquals(timestamp, domainHolding.timestamp)
    }

    //===========================================================//

    @Test
    fun `newEntity converts domain to entity correctly`() {
        val id = UUID.randomUUID()
        val capital = 15_000.0
        val timestamp = Instant.now()
        val holdingId = UUID.randomUUID()
        val portfolio = PortfolioEntity(id = UUID.randomUUID())

        val domain = createDomainTrader(
            id = id,
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

        val entity = domain.toEntity(portfolio)

        assertEquals(id, entity.id)
        assertEquals(capital, entity.capital)
        assertEquals(portfolio, entity.portfolio)
        assertEquals(securityIdentifierEntity.isin, entity.securityIdentifier.isin)
        assertEquals(1, entity.holdings.size)

        val entityHolding = entity.holdings.first()
        assertEquals(holdingId, entityHolding.id)
        assertEquals(120.0, entityHolding.entryPrice)
        assertEquals(8, entityHolding.amount)
        assertEquals(timestamp, entityHolding.timestamp)
        assertEquals(entity, entityHolding.trader)   // back-reference
    }

    // TODO : Add so what happens if the algorithm changes.
}