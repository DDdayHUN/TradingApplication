package data

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.nio.charset.StandardCharsets

//===========================================================//
//===========================================================//

object SerializationManager {
    //===========================================================//
    //===========================================================//
    // Private Field(s)

    private val s_GSON: Gson = GsonBuilder()
        .setPrettyPrinting()
        .create()

    //===========================================================//
    //===========================================================//
    // Public Interface(s)

    fun saveToFile(file: File, serData: SerializationData?) {
        if (!file.exists()) file.createNewFile()
        OutputStreamWriter(FileOutputStream(file), StandardCharsets.UTF_8).use { writer ->
            s_GSON.toJson(serData, writer)
        }
    }

    //===========================================================//

    fun loadFromFile(file: File): SerializationData {
        InputStreamReader(FileInputStream(file), StandardCharsets.UTF_8).use { reader ->
            return s_GSON.fromJson(reader, SerializationData::class.java)
        }
    }
}
