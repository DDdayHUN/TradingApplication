package data.repository.util

import com.google.gson.Gson
import java.io.File
import java.io.FileInputStream
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets

internal object RepositoryUtil {
    inline fun <reified T> loadFromFile(serializer: Gson, file: File): T {
        InputStreamReader(FileInputStream(file), StandardCharsets.UTF_8).use { reader ->
            return serializer.fromJson(reader, T::class.java)
        }
    }
}