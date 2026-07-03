package data.repository.trader

import com.google.gson.GsonBuilder
import domain.algorithm.TradingAlgorithm
import domain.assets.security.SecurityIdentifier
import domain.trader.Trader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import java.io.File

@Deprecated("Fake repo")
class FakeTraderRepository() : ITraderRepository {

    //===========================================================//
    //===========================================================//
    // Private Field(s)
    private val s_DirectoryPath: String = "src/main/resources/traders"
    private val s_Gson = GsonBuilder().setPrettyPrinting().create()


    //===========================================================//
    //===========================================================//
    // Public Method(s)
    override suspend fun save(trader: Trader, algorithmType: TradingAlgorithm.Type) = withContext(Dispatchers.IO) {
        val directory = File(s_DirectoryPath)
        directory.mkdirs()

        val dto = TraderDto(
            trader.securityIdentifier,
            trader.getHoldings(),
            trader.getCurrentCapital(),
            algorithmType.toString()
        )

        val file = File(s_DirectoryPath, "${trader.securityIdentifier.isin}.json")

        file.writeText(s_Gson.toJson(dto))
    }

    override suspend fun load(securityIdentifier: SecurityIdentifier): Trader? = withContext(Dispatchers.IO) {
        val file = File(s_DirectoryPath, "${securityIdentifier.isin}.json")

        if (!file.exists()) {
            return@withContext null
        }

        if (file.readText().isBlank()) {
            return@withContext null
        }

        return@withContext s_Gson.fromJson(file.readText(), TraderDto::class.java).toDomain()
    }

    override suspend fun loadAll(): List<Trader> = withContext(Dispatchers.IO) {
        val directory = File(s_DirectoryPath)

        if (!directory.exists()) {
            return@withContext emptyList()
        }

        val files = directory.listFiles()?: return@withContext emptyList()

        return@withContext files.mapNotNull { file ->

            if (file.readText().isBlank()) {
                null
            } else {
                s_Gson.fromJson(file.readText(), TraderDto::class.java).toDomain()
            }
        }
    }

    //===========================================================//
    //===========================================================//
    // Private Method(s)
    private fun TraderDto.toDomain(): Trader {
        return Trader(
            securityIdentifier,
            holdings.toMutableList(),
            allocatedCapital,
            parseAlgorithmType(algorithmType)
        )
    }

    private fun parseAlgorithmType(value: String): TradingAlgorithm.Type {
        return when (value) {
            "TACPP46" -> TradingAlgorithm.Type.TACPP46
            else -> throw IllegalArgumentException("Unknown algorithm type: $value")
        }
    }
}