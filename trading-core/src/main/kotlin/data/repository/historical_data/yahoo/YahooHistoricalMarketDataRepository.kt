package data.repository.historical_data.yahoo

import com.google.gson.GsonBuilder
import data.repository.utils.RepositoryUtils
import domain.interfaces.IHistoricalMarketDataProvider
import domain.market.security.SecurityHistory
import domain.market.security.SecurityIdentifier
import kotlinx.coroutines.*
import java.io.File
import kotlin.time.Instant

internal object YahooHistoricalMarketDataRepository : IHistoricalMarketDataProvider {
    //===========================================================//
    //===========================================================//
    // Private Field(s)

    private val s_RootDir = run {
        val resource = javaClass.getResource("/backtest/yahoo/")
            ?: error("Resource directory '/backtest/yahoo/' not found")

        File(resource.toURI())
    }

    private val s_GSON = GsonBuilder()
        .enableComplexMapKeySerialization()
        .setPrettyPrinting()
        .create()

    //===========================================================//
    //===========================================================//
    // Public Method(es)

    override suspend fun getBySecurityIdentifier(securityIdentifier: SecurityIdentifier, from: Instant, to: Instant): Result<List<SecurityHistory>> {
        try {
            val data = getBySecurityIdentifier(securityIdentifier)

            val ret = data.history
                .filter { it.date in from..to }
                .sortedBy { it.date }
                .map { SecurityHistory(it.closingPrice) }
                .toMutableList()

            return Result.success(ret)
        }
        catch (e: Exception) {
            return Result.failure(e)
        }
    }

    //===========================================================//

    @Deprecated("We need to redo this, because this is too expensive")
    override suspend fun getAllSecurityIdentifiers(): Result<List<SecurityIdentifier>> {
        try {
            val data = getAll()
            val ret = data
                .map {
                    SecurityIdentifier(it.meta.isin, it.meta.tickerSymbol, it.meta.currency)
                }
            return Result.success(ret)
        }
        catch (e: Exception) {
            return Result.failure(e)
        }
    }

    //===========================================================//
    //===========================================================//
    // Private Method(es)

    private suspend fun getBySecurityIdentifier(securityIdentifier: SecurityIdentifier): HistoricalMarketDataDto = withContext(Dispatchers.IO) {
        val targetFile = s_RootDir.walkTopDown()
            .filter { it.isFile }
            .find {
                val yahooMarketDataDto = RepositoryUtils.loadFromFile<YahooMarketDataDto>(s_GSON, it)
                yahooMarketDataDto.isin == securityIdentifier.isin
            }

        require(targetFile != null) { "There is no file with the given identifier" }
        return@withContext RepositoryUtils.loadFromFile<YahooMarketDataDto>(s_GSON, targetFile).toHistoricalMarketDataDto()
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
                    RepositoryUtils
                        .loadFromFile<YahooMarketDataDto>(s_GSON, it)
                        .toHistoricalMarketDataDto()
                }
            }.awaitAll()
        }
    }
}