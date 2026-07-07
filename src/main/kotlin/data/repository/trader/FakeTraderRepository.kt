package data.repository.trader

import com.google.gson.GsonBuilder
import data.repository.utils.RepositoryUtils
import domain.market.security.SecurityIdentifier
import domain.interfaces.ITraderRepository
import domain.trader.Trader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import java.io.File

@Deprecated("Fake repo")
internal object FakeTraderRepository : ITraderRepository {

    //===========================================================//
    //===========================================================//
    // Private Field(s)

    private val s_DirectoryPath = File("src/main/resources/traders/test")
    private val s_Gson = GsonBuilder()
        .enableComplexMapKeySerialization()
        .setPrettyPrinting()
        .create()

    //===========================================================//
    //===========================================================//
    // Public Method(s)

    override suspend fun save(trader: Trader) = withContext(Dispatchers.IO) {
        val dto = TraderDto(
            trader.uuid,
            trader.securityIdentifier,
            trader.capital,
            trader.holdings,
            trader.algorithm,
        )

        if (!s_DirectoryPath.exists()) {
            s_DirectoryPath.mkdirs()
        }

        val file = File(s_DirectoryPath, "${trader.uuid}.json")

        RepositoryUtils.saveToFile<TraderDto>(s_Gson, file, dto)
    }

    //===========================================================//

    override suspend fun getBySecurityIdentifier(securityIdentifier: SecurityIdentifier): Trader =
        withContext(Dispatchers.IO) {
            val file = File(s_DirectoryPath, "${securityIdentifier.isin}.json")

            require(file.isFile) { "There is no file with the given identifier" }

            val dto = RepositoryUtils.loadFromFile<TraderDto>(s_Gson, file)

            return@withContext dto.toDomain()
        }

    //===========================================================//

    override suspend fun getAll(): List<Trader> = withContext(Dispatchers.IO) {
        val files = s_DirectoryPath.listFiles() ?: return@withContext listOf()

        coroutineScope {
            files.filter { it.isFile }
                .map {
                    async {
                        RepositoryUtils.loadFromFile<TraderDto>(s_Gson, it)
                            .toDomain()
                    }
                }.awaitAll()
        }
    }
}