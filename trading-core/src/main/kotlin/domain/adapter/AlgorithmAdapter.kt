package domain.adapter

import com.google.gson.*
import domain.algorithm.*
import java.lang.reflect.Type

class AlgorithmAdapter : JsonSerializer<TradingAlgorithm>, JsonDeserializer<TradingAlgorithm> {
    override fun serialize(src: TradingAlgorithm, typeOfT: Type, context: JsonSerializationContext): JsonElement {
        val jsonElement = context.serialize(src, src.javaClass).asJsonObject
        val typeTag = TradingAlgorithm.typeTagOf(src).getOrElse {
            throw JsonParseException("Failed to serialize algorithm: unknown implementation class ${src::class.java.simpleName}", it)
        }

        jsonElement.addProperty("algorithmType", typeTag)

        return jsonElement
    }

    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): TradingAlgorithm {
        val jsonObject = json.asJsonObject
        val typeTag = jsonObject.get("algorithmType")?.asString
            ?: throw JsonParseException("Missing 'algorithmType' field in algorithm payload")

        val targetClass = TradingAlgorithm.classFromTag(typeTag).getOrElse { exception ->
            throw JsonParseException("Unknown algorithm type tag: $typeTag", exception)
        }

        return context.deserialize(jsonObject, targetClass)
    }
}

