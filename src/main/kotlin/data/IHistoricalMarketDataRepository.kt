package data

import com.google.gson.Gson
import domain.assets.security.SecurityIdentifier
import java.io.File
import java.io.FileInputStream
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets

internal interface IHistoricalMarketDataRepository {
    fun get(securityIdentifier: SecurityIdentifier): SecuritySerializationData

    companion object {
        inline fun <reified T> loadFromFile(serializer: Gson, file: File): T {
            InputStreamReader(FileInputStream(file), StandardCharsets.UTF_8).use { reader ->
                return serializer.fromJson(reader, T::class.java)
            }
        }
    }
}