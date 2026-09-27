package data.repository.historical_data.json.yahoo

import com.google.gson.GsonBuilder
import data.repository.historical_data.HistoricalMarketDataDto
import data.repository.historical_data.IHistoricalMarketDataProvider
import data.repository.loadFromFile
import domain.market.security.SecurityHistory
import domain.market.security.SecurityIdentifier
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

    private val s_GSON = GsonBuilder()
        .enableComplexMapKeySerialization()
        .setPrettyPrinting()
        .create()


    private val data: Map<String, HistoricalMarketDataDto> by lazy {

        s_RootDir
            .walkTopDown()
            .filter { it.isFile }
            .map {
                loadFromFile<YahooMarketDataDto>(
                    s_GSON,
                    it
                ).toHistoricalMarketDataDto()
            }
            .associateBy {
                it.meta.isin
            }
    }

    //===========================================================//
    //===========================================================//
    // Public Method(es)

    override suspend fun getBySecurityIdentifier(securityIdentifier: SecurityIdentifier, from: Instant, to: Instant): Result<List<SecurityHistory>> {
        return runCatching {
            val securityData = requireNotNull(data[securityIdentifier.isin]){"There is no file with identifier ${securityIdentifier}"}

            securityData.history
                .asSequence()
                .filter { security -> security.date in from..to}
                .sortedBy { security -> security.date }
                .map { security -> SecurityHistory(security.price) }
                .toList()
        }
    }

    //===========================================================//

    override suspend fun getAllSecurityIdentifiers(): Result<List<SecurityIdentifier>> {
        return runCatching {
            data.values.map { security ->
                SecurityIdentifier(
                    isin = security.meta.isin,
                    tickerSymbol = security.meta.tickerSymbol,
                    currency = security.meta.currency,
                )
            }
        }
    }
}