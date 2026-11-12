package data.repository.historical_data.json.yahoo

import data.repository.historical_data.HistoricalMarketDataDto
import data.repository.historical_data.IHistoricalMarketDataProvider
import data.repository.loadFromFile
import domain.market.security.SecurityHistory
import domain.market.security.SecurityIdentifier
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component
import java.io.File
import kotlin.lazy
import kotlin.time.Instant

@Component
@Qualifier("yahoo")
internal object YahooHistoricalMarketDataRepository : IHistoricalMarketDataProvider {
    //===========================================================//
    //===========================================================//
    // Private Field(s)

    private val s_RootDir = run {
        val resource = javaClass.getResource("/backtest/yahoo/us")
            ?: error("Resource directory '/backtest/yahoo/' not found")

        File(resource.toURI())
    }

    // TODO : Better than previous one, but i believe this can still be better.
    private val s_SecurityToFileMap: Map<SecurityIdentifier, File> by lazy {
        s_RootDir
            .walkTopDown()
            .filter { it.isFile }
            .associateBy { file ->
                val dto = loadFromFile<YahooMarketDataDto>(file).toHistoricalMarketDataDto()
                SecurityIdentifier(dto.meta.isin, dto.meta.tickerSymbol, dto.meta.currency)
            }
    }

    //===========================================================//
    //===========================================================//
    // Public Method(s)

    override suspend fun getBySecurityIdentifier(
        securityIdentifier: SecurityIdentifier,
        from: Instant,
        to: Instant
    ): Result<List<SecurityHistory>> {
        return runCatching {
            val data = getBySecurityIdentifier(securityIdentifier)

            data.history
                .filter { it.date in from..to }
                .sortedBy { it.date }
                .map { SecurityHistory(it.price, it.date) }
                .toList()
        }
    }

    //===========================================================//

    override suspend fun getAllSecurityIdentifiers(): Result<List<SecurityIdentifier>> {
        return runCatching {
            s_SecurityToFileMap.keys.toList()
        }
    }

    //===========================================================//
    //===========================================================//
    // Private Method(s)

    private fun getBySecurityIdentifier(securityIdentifier: SecurityIdentifier): HistoricalMarketDataDto {
        val targetFile = s_SecurityToFileMap[securityIdentifier]

        require(targetFile != null) { "There is no file with the given identifier" }
        return loadFromFile<YahooMarketDataDto>(targetFile).toHistoricalMarketDataDto()
    }
}