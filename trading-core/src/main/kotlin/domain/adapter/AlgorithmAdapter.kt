package domain.adapter

import com.google.gson.*
import domain.algorithm.*
import java.lang.reflect.Type

class AlgorithmAdapter : JsonSerializer<ITradingAlgorithm>, JsonDeserializer<ITradingAlgorithm> {
    override fun serialize(src: ITradingAlgorithm, typeOfT: Type, context: JsonSerializationContext): JsonElement {
        val jsonElement = context.serialize(src, src.javaClass).asJsonObject
        val typeTag = ITradingAlgorithm.typeTagOf(src).getOrElse {
            throw JsonParseException("Failed to serialize algorithm: unknown implementation class ${src::class.java.simpleName}", it)
        }

        jsonElement.addProperty("algorithmType", typeTag)

        return jsonElement
    }

    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): ITradingAlgorithm {
        val jsonObject = json.asJsonObject
        val typeTag = jsonObject.get("algorithmType")?.asString
            ?: throw JsonParseException("Missing 'algorithmType' field in algorithm payload")

        val targetClass = ITradingAlgorithm.classFromTag(typeTag).getOrElse { exception ->
            throw JsonParseException("Unknown algorithm type tag: $typeTag", exception)
        }

        return context.deserialize(jsonObject, targetClass)
    }
}

