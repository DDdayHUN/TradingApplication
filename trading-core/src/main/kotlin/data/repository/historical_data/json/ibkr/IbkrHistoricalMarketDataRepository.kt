package data.repository.historical_data.json.ibkr

import com.google.gson.GsonBuilder
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
import java.io.File
import kotlin.time.Instant

internal object IbkrHistoricalMarketDataRepository : IHistoricalMarketDataProvider {
    //===========================================================//
    //===========================================================//
    // Private Field(s)

    private val s_RootDir = run {
        val resource = javaClass.getResource("/backtest/ibkr/")
            ?: error("Resource directory '/backtest/ibkr/' not found")

        File(resource.toURI())
    }

    private val s_GSON = GsonBuilder()
        .enableComplexMapKeySerialization()
        .setPrettyPrinting()
        .create()

    //===========================================================//
    //===========================================================//
    // Public Method(es)

    @Suppress("DuplicatedCode")
    override suspend fun getBySecurityIdentifier(
        securityIdentifier: SecurityIdentifier,
        from: Instant,
        to: Instant
    ): Result<List<SecurityHistory>> {
        try {
            val data = getBySecurityIdentifier(securityIdentifier)

            val ret = data.history
                .filter { it.date in from..to }
                .sortedBy { it.date }
                .map { SecurityHistory(it.price) }
                .toMutableList()

            return Result.success(ret)
        }
        catch (e: Exception) {
            return Result.failure(e)
        }
    }

    //===========================================================//

    @Deprecated("We need to redo this, because this is too expensive")
    @Suppress("DuplicatedCode")
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
                val yahooMarketDataDto = loadFromFile<IbkrMarketDataDto>(s_GSON, it)
                yahooMarketDataDto.isin == securityIdentifier.isin
            }

        require(targetFile != null) { "There is no file with the given identifier" }
        return@withContext loadFromFile<IbkrMarketDataDto>(s_GSON, targetFile).toHistoricalMarketDataDto()
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
                    loadFromFile<IbkrMarketDataDto>(s_GSON, it)
                        .toHistoricalMarketDataDto()
                }
            }.awaitAll()
        }
    }
}