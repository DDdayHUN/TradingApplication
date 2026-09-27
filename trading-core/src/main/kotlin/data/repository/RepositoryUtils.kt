package data.repository

import com.google.gson.GsonBuilder
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.nio.charset.StandardCharsets

@PublishedApi
internal val g_GSON = GsonBuilder()
    .enableComplexMapKeySerialization()
    .setPrettyPrinting()
    .create()

inline fun <reified T> loadFromFile(file: File): T {
    InputStreamReader(FileInputStream(file), StandardCharsets.UTF_8).use { reader ->
        return g_GSON.fromJson(reader, T::class.java)
    }
}

inline fun <reified T> saveToFile(file: File, obj: T) {
    OutputStreamWriter(FileOutputStream(file), StandardCharsets.UTF_8).use { writer ->
        g_GSON.toJson(obj, T::class.java, writer)
    }
}