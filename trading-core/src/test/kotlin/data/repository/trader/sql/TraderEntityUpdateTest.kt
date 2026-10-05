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

    private fun createTraderEntity(
        id: UUID = traderId,
        capital: Double = 10_000.0,
        holdings: MutableSet<SecurityHoldingEntity> = mutableSetOf()
    ): TraderEntity {
        return TraderEntity(
            id = id,
            securityIdentifier = securityIdentifierEntity,
            portfolio = portfolio,
            capital = capital,
            algorithmType = "BUYANDHOLD",
            algorithmState = "{}"
        ).also { it.holdings.addAll(holdings) }
    }

    //===========================================================//

    private fun createDomainTrader(
        id: UUID = traderId,
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

    private fun createHoldingEntity(
        id: UUID,
        entryPrice: Double,
        amount: Int,
        timestamp: Instant,
        trader: TraderEntity
    ): SecurityHoldingEntity {
        return SecurityHoldingEntity(
            id = id,
            entryPrice = entryPrice,
            amount = amount,
            timestamp = timestamp,
            trader = trader
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
    fun `update copies scalar fields`() {
        val entity = createTraderEntity(capital = 5_000.0)
        val domain = createDomainTrader(capital = 12_500.0)

        entity.update(domain)

        assertEquals(12_500.0, entity.capital)
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
                createHoldingEntity(holdingId1, 100.0, 10, timestamp1, entity),
                createHoldingEntity(holdingId2, 200.0, 5, timestamp2, entity)
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
            createHoldingEntity(holdingId, 100.0, 10, oldTimestamp, entity)
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
            createHoldingEntity(existingId, 100.0, 10, timestamp, entity)
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
}