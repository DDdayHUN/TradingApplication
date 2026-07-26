package domain.algorithm

import domain.market.security.SecurityHistory
import domain.market.security.SecurityHolding
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@DisplayName("TACPP46 trading algorithm")
internal class TACPP46Test {

    companion object {
        private const val SLIDING_WINDOW = 21
        private const val REQUIRED_HISTORY_SIZE = SLIDING_WINDOW * 2
        private const val DEFAULT_PRICE = 100.0
        private const val DEFAULT_CAPITAL = 10_000.0
    }

    // =========================================================
    // Constructor tests
    // =========================================================

    @Nested
    @DisplayName("Constructor")
    inner class ConstructorTests {

        @Test
        fun `constructor rejects empty history`(){
            val exception = assertFailsWith<IllegalArgumentException> {
                TACPP46(emptyList())
            }

            assertTrue(
                exception.message.orEmpty().contains("Init EMA")
            )
        }

        @Test
        fun `constructor rejects one history entry`(){
            val history = flatHistory(
                count = 1,
                price = DEFAULT_PRICE,
            )

            assertFailsWith<IllegalArgumentException>{
                TACPP46(history)
            }
        }

        @Test
        fun `constructor rejects less history then required`() {
            val history = flatHistory(
                count = REQUIRED_HISTORY_SIZE - 1,
                price = DEFAULT_PRICE,
            )

            assertFailsWith<IllegalArgumentException> {
                TACPP46(history)
            }
        }

        @Test
        fun `constructor accepts exactly history then required`() {
            val history = flatHistory(
                count = REQUIRED_HISTORY_SIZE,
                price = DEFAULT_PRICE,
            )

            val algorithm = TACPP46(history)

            assertNotNull(algorithm)
        }

        @Test
        fun `constructor accepts more then required history`() {
            val history = flatHistory(
                count = 100,
                price = DEFAULT_PRICE,
            )

            val algorithm = TACPP46(history)

            assertNotNull(algorithm)
        }

        @Test
        fun `constructor accepts increasing history`() {
            val history = increasingHistory(
                count = REQUIRED_HISTORY_SIZE,
                firstPrice = 50.0,
                increasePerEntry = 1.0
            )

            val algorithm = TACPP46(history)

            assertNotNull(algorithm)
        }

        @Test
        fun `constructor accepts decreasing history`() {
            val history = decreasingHistory(
                count = REQUIRED_HISTORY_SIZE,
                firstPrice = 100.0,
                decreasePerEntry = 1.0
            )

            val algorithm = TACPP46(history)

            assertNotNull(algorithm)
        }

        @Test
        fun `constructor accepts history containing zero prices`() {
            val history = flatHistory(
                count = REQUIRED_HISTORY_SIZE,
                price = 0.0
            )

            val algorithm = TACPP46(history)

            assertNotNull(algorithm)
        }
    }

    // =========================================================
    // Basic output tests
    // =========================================================

    @Nested
    @DisplayName("Basic output")
    inner class BasicOutputTests {

        @Test
        fun `run returns output when holdings are empty`() {
            val algorithm = createFlatAlgorithm()

            val output = algorithm.run(
                holdings = emptyList(),
                allocatedCapital = DEFAULT_CAPITAL,
                currentPrice = DEFAULT_PRICE
            )

            assertNotNull(output)
        }

        @Test
        fun `run does not produce sell output when holdings are empty`() {
            val algorithm = createFlatAlgorithm()

            val output = algorithm.run(
                holdings = emptyList(),
                allocatedCapital = DEFAULT_CAPITAL,
                currentPrice = DEFAULT_PRICE
            )

            assertNull(output.sell)
        }

        @Test
        fun `run does not sell a holding when price equals entry price`() {
            val algorithm = createFlatAlgorithm()
            val holding = defaultHolding()

            val output = algorithm.run(
                holdings = listOf(holding),
                allocatedCapital = DEFAULT_CAPITAL,
                currentPrice = holding.entryPrice
            )

            assertNull(output.sell)
        }

        @Test
        fun `run does not mutate supplied holdings list`(){
            val algorithm = createFlatAlgorithm()

            val firstHolding = SecurityHolding(
                entryPrice = 100.0,
                amount = 5
            )

            val secondHolding = SecurityHolding(
                entryPrice = 200.0,
                amount = 3
            )

            val holdings = mutableListOf(firstHolding, secondHolding)

            val holdingsBeforeRun = holdings.toList()

            algorithm.run(
                holdings = holdings,
                allocatedCapital = DEFAULT_CAPITAL,
                currentPrice = 100.0
            )

            assertEquals(
                holdingsBeforeRun,
                holdings
            )
        }

        @Test
        fun `run can be called repeatedly`() {
            val algorithm = createFlatAlgorithm()
            val holding = defaultHolding()

            repeat(10) {
                val output = algorithm.run(
                    holdings = listOf(holding),
                    allocatedCapital = DEFAULT_CAPITAL,
                    currentPrice = DEFAULT_PRICE
                )

                assertNotNull(output)
            }
        }

        @Test
        fun `separate algorithm instances maintain separate state`() {
            val firstAlgorithm = createFlatAlgorithm()
            val secondAlgorithm = createFlatAlgorithm()

            val holding = defaultHolding()

            firstAlgorithm.run(
                holdings = listOf(holding),
                allocatedCapital = DEFAULT_CAPITAL,
                currentPrice = 150.0
            )

            val firstOutput = firstAlgorithm.run(
                holdings = listOf(holding),
                allocatedCapital = DEFAULT_CAPITAL,
                currentPrice = 100.0
            )

            val secondOutput = secondAlgorithm.run(
                holdings = listOf(holding),
                allocatedCapital = DEFAULT_CAPITAL,
                currentPrice = 100.0
            )

            assertNotNull(firstOutput.sell)
            assertNull(secondOutput.sell)
        }
    }

    // =========================================================
    // Helper Methods
    // =========================================================

    private fun flatHistory(
        count: Int,
        price: Double
    ): List<SecurityHistory>{
        return List(count){
            SecurityHistory(
                closingPrice = price
            )
        }
    }

    private fun defaultHolding(): SecurityHolding {
        return SecurityHolding(
            entryPrice = DEFAULT_PRICE,
            amount = 5
        )
    }

    private fun createFlatAlgorithm(
        price: Double = DEFAULT_PRICE
    ): TACPP46{
        return TACPP46(
            flatHistory(
                count = REQUIRED_HISTORY_SIZE,
                price = price
            )
        )
    }

    private fun increasingHistory(
        count: Int,
        firstPrice: Double,
        increasePerEntry: Double
    ): List<SecurityHistory> {
        return List(count) { index ->
            SecurityHistory(
                closingPrice = firstPrice + (index * increasePerEntry)
            )
        }
    }

    private fun decreasingHistory(
        count: Int,
        firstPrice: Double,
        decreasePerEntry: Double
    ): List<SecurityHistory> {
        return List(count) { index ->
            SecurityHistory(
                closingPrice = (firstPrice - (index * decreasePerEntry)).coerceAtLeast(0.01)
            )
        }
    }
}