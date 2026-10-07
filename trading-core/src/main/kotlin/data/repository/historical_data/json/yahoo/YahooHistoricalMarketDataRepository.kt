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

    @Deprecated("Ez is eléggé veszélyes, hogyha nagy a file állományunk!")
    private val s_Data: Map<String, HistoricalMarketDataDto> by lazy {
        s_RootDir
            .walkTopDown()
            .filter { it.isFile }
            .map {
                loadFromFile<YahooMarketDataDto>(it).toHistoricalMarketDataDto()
            }
            .associateBy {
                it.meta.isin
            }
    }

    //===========================================================//
    //===========================================================//
    // Public Method(es)

    override suspend fun getBySecurityIdentifier(
        securityIdentifier: SecurityIdentifier,
        from: Instant,
        to: Instant
    ): Result<List<SecurityHistory>> {
        return runCatching {
            /*
            val data = getBySecurityIdentifier(securityIdentifier)

            data.history
                .filter { it.date in from..to }
                .sortedBy { it.date }
                .map { SecurityHistory(it.price, it.date) }
                .toList()*/
            val securityData = requireNotNull(s_Data[securityIdentifier.isin]){"There is no file with identifier $securityIdentifier"}

            securityData.history
                .asSequence()
                .filter { security -> security.date in from..to}
                .sortedBy { security -> security.date }
                .map { security -> SecurityHistory(security.price, security.date) }
                .toList()
        }
    }

    //===========================================================//

    override suspend fun getAllSecurityIdentifiers(): Result<List<SecurityIdentifier>> {
        return runCatching {
            /*
            val data = getAll()
            data.map {
                SecurityIdentifier(it.meta.isin, it.meta.tickerSymbol, it.meta.currency)
            }*/

            s_Data.values.map { security ->
                SecurityIdentifier(
                    isin = security.meta.isin,
                    tickerSymbol = security.meta.tickerSymbol,
                    currency = security.meta.currency,
                )
            }
        }
    }

    //===========================================================//
    //===========================================================//
    // Private Method(es)

    /*
    private suspend fun getBySecurityIdentifier(securityIdentifier: SecurityIdentifier): HistoricalMarketDataDto = withContext(Dispatchers.IO) {
        val targetFile = s_RootDir.walkTopDown()
            .filter { it.isFile }
            .find {
                val yahooMarketDataDto = loadFromFile<YahooMarketDataDto>(it)
                yahooMarketDataDto.isin == securityIdentifier.isin
            }

        require(targetFile != null) { "There is no file with the given identifier" }
        return@withContext loadFromFile<YahooMarketDataDto>(targetFile).toHistoricalMarketDataDto()
    }

    //===========================================================//

    private suspend fun getAll(): List<HistoricalMarketDataDto> = withContext(Dispatchers.IO) {
        val files = s_RootDir
            .walkTopDown()
            .filter { it.isFile }
            .toList()

        coroutineScope {
            files.map {
                async {
                    loadFromFile<YahooMarketDataDto>(it)
                        .toHistoricalMarketDataDto()
                }
            }.awaitAll()
        }
    }*/
}