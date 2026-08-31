package data.repository.utils

import com.google.gson.Gson
import java.io.*
import java.nio.charset.StandardCharsets

internal object RepositoryUtils {
    inline fun <reified T> loadFromFile(serializer: Gson, file: File): T {
        InputStreamReader(FileInputStream(file), StandardCharsets.UTF_8).use { reader ->
            return serializer.fromJson(reader, T::class.java)
        }
    }

    inline fun <reified T> saveToFile(serializer: Gson, file: File, obj: T) {
        OutputStreamWriter(FileOutputStream(file), StandardCharsets.UTF_8).use { writer ->
            serializer.toJson(obj, T::class.java, writer)
        }
    }
}