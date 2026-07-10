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

    override suspend fun save(trader: Trader): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val dto = TraderDto(
                trader.uuid,
                trader.securityIdentifier,
                trader.capital,
                trader.holdings,
                trader.algorithm,
            )

            if (!s_DirectoryPath.exists()) s_DirectoryPath.mkdirs()

            val file = File(s_DirectoryPath, "${trader.uuid}.json")

            RepositoryUtils.saveToFile<TraderDto>(s_Gson, file, dto)
            return@withContext Result.success(Unit)
        }
        catch(e: Exception) {
            return@withContext Result.failure(e)
        }
    }

    //===========================================================//

    override suspend fun getBySecurityIdentifier(securityIdentifier: SecurityIdentifier): Result<Trader> = withContext(Dispatchers.IO) {
        try {
            val file = File(s_DirectoryPath, "${securityIdentifier.isin}.json")
            val dto = RepositoryUtils.loadFromFile<TraderDto>(s_Gson, file)
            return@withContext Result.success(dto.toDomain())
        }
        catch(e: Exception) {
            return@withContext Result.failure(e)
        }
    }

    //===========================================================//

    override suspend fun getAll(): Result<List<Trader>> = withContext(Dispatchers.IO) {
        try {
            val files = s_DirectoryPath.listFiles() ?: throw Exception("There is no directory")

            val ret = coroutineScope {
                files.filter { it.isFile }
                    .map {
                        async {
                            RepositoryUtils.loadFromFile<TraderDto>(s_Gson, it)
                                .toDomain()
                        }
                    }.awaitAll()
                }
            return@withContext Result.success(ret)
        }
        catch(e: Exception) {
            return@withContext Result.failure(e)
        }
    }
}