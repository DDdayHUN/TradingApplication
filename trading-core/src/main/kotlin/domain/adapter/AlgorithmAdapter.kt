package domain.adapter

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import domain.algorithm.ALGDES2
import domain.algorithm.ALGDES3
import domain.algorithm.ALGDES31
import domain.algorithm.ALGDES4
import domain.algorithm.BUYANDHOLD
import domain.algorithm.ITradingAlgorithm
import domain.algorithm.TACPP46
import domain.algorithm.TACPP462
import java.lang.reflect.Type

class AlgorithmAdapter : JsonSerializer<ITradingAlgorithm>, JsonDeserializer<ITradingAlgorithm> {
    override fun serialize(src: ITradingAlgorithm, typeOfT: Type, context: JsonSerializationContext): JsonElement {
        val jsonElement = context.serialize(src, src.javaClass).asJsonObject

        jsonElement.addProperty("algorithmType", ITradingAlgorithm.typeTagOf(src))

        return jsonElement
    }

    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): ITradingAlgorithm {
        val jsonObject = json.asJsonObject
        val typeTag = jsonObject.get("algorithmType")?.asString ?: throw JsonParseException("Missing 'algorithmType' field in algorithm payload")

        return when (typeTag) {
            "TACPP46" -> context.deserialize(jsonObject, TACPP46::class.java)
            "ALGDES2" -> context.deserialize(jsonObject, ALGDES2::class.java)
            "ALGDES3" -> context.deserialize(jsonObject, ALGDES3::class.java)
            "ALGDES31" -> context.deserialize(jsonObject, ALGDES31::class.java)
            "ALGDES4" -> context.deserialize(jsonObject, ALGDES4::class.java)
            "BUYANDHOLD" -> context.deserialize(jsonObject, BUYANDHOLD::class.java)
            "TACPP462" -> context.deserialize(jsonObject, TACPP462::class.java)
            else -> throw JsonParseException("Unknown algorithm type tag: $typeTag")
        }
    }
}

