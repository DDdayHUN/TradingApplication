package data.repository.historical_data.json.ibkr

import data.repository.historical_data.HistoricalMarketDataDto
import data.repository.historical_data.IHistoricalMarketDataProvider
import data.repository.loadFromFile
import domain.market.security.SecurityHistory
import domain.market.security.SecurityIdentifier
import kotlinx.coroutines.*
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component
import java.io.File
import kotlin.time.Instant

@Component
@Qualifier("ibkr")
internal object IbkrHistoricalMarketDataRepository : IHistoricalMarketDataProvider {
    //===========================================================//
    //===========================================================//
    // Private Field(s)

    private val s_RootDir = run {
        val resource = javaClass.getResource("/backtest/ibkr/")
            ?: error("Resource directory '/backtest/ibkr/' not found")

        File(resource.toURI())
    }

    //===========================================================//
    //===========================================================//
    // Public Method(es)

    @Suppress("DuplicatedCode")
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

    @Deprecated("We need to redo this, because this is too expensive")
    @Suppress("DuplicatedCode")
    override suspend fun getAllSecurityIdentifiers(): Result<List<SecurityIdentifier>> {
        return runCatching {
            val data = getAll()
            data.map {
                SecurityIdentifier(it.meta.isin, it.meta.tickerSymbol, it.meta.currency)
            }
        }
    }

    //===========================================================//
    //===========================================================//
    // Private Method(es)

    @Suppress("DuplicatedCode")
    private suspend fun getBySecurityIdentifier(securityIdentifier: SecurityIdentifier): HistoricalMarketDataDto = withContext(Dispatchers.IO) {
        val targetFile = s_RootDir.walkTopDown()
            .filter { it.isFile }
            .find {
                val yahooMarketDataDto = loadFromFile<IbkrMarketDataDto>(it)
                yahooMarketDataDto.isin == securityIdentifier.isin
            }

        require(targetFile != null) { "There is no file with the given identifier" }
        return@withContext loadFromFile<IbkrMarketDataDto>(targetFile).toHistoricalMarketDataDto()
    }

    //===========================================================//

    @Suppress("DuplicatedCode")
    private suspend fun getAll(): List<HistoricalMarketDataDto> = withContext(Dispatchers.IO) {
        val files = s_RootDir
            .walkTopDown()
            .filter { it.isFile }
            .toList()

        coroutineScope {
            files.map {
                async {
                    loadFromFile<IbkrMarketDataDto>(it)
                        .toHistoricalMarketDataDto()
                }
            }.awaitAll()
        }
    }
}