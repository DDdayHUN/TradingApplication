package data.repository.historical_data.yahoo

import com.google.gson.GsonBuilder
import data.repository.historical_data.HistoricalMarketDataDto
import data.repository.historical_data.IHistoricalMarketDataRepository
import data.repository.utils.RepositoryUtils
import domain.market.security.SecurityIdentifier
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import java.io.File

internal object YahooHistoricalMarketDataRepository : IHistoricalMarketDataRepository {
    //===========================================================//
    //===========================================================//
    // Private Field(s)

    private val s_RootDir = File("src/main/resources/backtest/yahoo/")

    private val s_GSON = GsonBuilder()
        .enableComplexMapKeySerialization()
        .setPrettyPrinting()
        .create()

    //===========================================================//
    //===========================================================//
    // Public Method(es)

    override suspend fun getBySecurityIdentifier(securityIdentifier: SecurityIdentifier): HistoricalMarketDataDto = withContext(Dispatchers.IO) {
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

    override suspend fun getAll(): List<HistoricalMarketDataDto> = withContext(Dispatchers.IO) {
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