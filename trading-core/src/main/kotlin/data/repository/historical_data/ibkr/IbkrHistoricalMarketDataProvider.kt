package data.repository.historical_data.ibkr

import application.service.borker.InteractiveBrokersService
import domain.interfaces.IHistoricalMarketDataProvider
import domain.market.security.SecurityHistory
import domain.market.security.SecurityIdentifier
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import kotlin.time.Instant

internal class IbkrHistoricalMarketDataProvider(
    private val service: InteractiveBrokersService
) : IHistoricalMarketDataProvider {
    override suspend fun getBySecurityIdentifier(
        securityIdentifier: SecurityIdentifier,
        from: Instant,
        to: Instant
    ): Result<List<SecurityHistory>> {
        try {
            val data = service.getHistoricalData(
                securityIdentifier,
                from,
                to
            )

            val ret = data
                .map {
                    Pair(
                        parseDate(it.date),
                        it.closingPrice
                    )
                }
                .filter {
                    it.first in from..to
                }
                .sortedBy {
                    it.first
                }
                .map {
                    SecurityHistory(it.second)
                }

            return Result.success(ret)
        }
        catch(e: Exception) {
            return Result.failure(e)
        }
    }

    override suspend fun getAllSecurityIdentifiers(): Result<List<SecurityIdentifier>> {
        return Result.failure(
            UnsupportedOperationException(
                "IBKR does not support retrieving all security identifiers."
            )
        )
    }

    //===========================================================//
    //===========================================================//
    // Private Method(es)

    private fun parseDate(date: String): Instant {
        val localDate = LocalDate.parse(
            date,
            DateTimeFormatter.BASIC_ISO_DATE
        )

        val javaInstant = localDate
            .atStartOfDay(ZoneOffset.UTC)
            .toInstant()

        return Instant.parse(
            javaInstant.toString()
        )
    }
}