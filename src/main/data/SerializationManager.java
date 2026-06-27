package data;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

//===========================================================//
//===========================================================//

public final class SerializationManager {
    //===========================================================//
    //===========================================================//
    // Static Field(s)

    static private final Gson s_GSON = new GsonBuilder()
        .setPrettyPrinting()
        .create();

    //===========================================================//
    //===========================================================//
    // Public Interface(s)

    static public void saveToFile(final File file, final SerializationData serData) throws IOException {
        if (!file.exists()) file.createNewFile();
        try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8)) 
        {
            s_GSON.toJson(serData, writer);
        }
    }

    //===========================================================//

    static public SerializationData loadFromFile(final File file) throws IOException {
        try (InputStreamReader reader = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8)) 
        {
            return s_GSON.fromJson(reader, SerializationData.class);
        }
    }

    //===========================================================//
    //===========================================================//
    // Constructor(s)

    private SerializationManager() {} // To prevent instantiation.
}
