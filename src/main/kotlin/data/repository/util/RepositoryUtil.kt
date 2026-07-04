package data.repository.util

import com.google.gson.Gson
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.nio.charset.StandardCharsets

internal object RepositoryUtil {
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