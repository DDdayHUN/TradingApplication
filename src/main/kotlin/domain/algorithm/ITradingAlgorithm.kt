package domain.algorithm

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import com.google.gson.annotations.JsonAdapter
import domain.algorithm.TradingAlgorithm.Output
import domain.market.security.SecurityHolding
import java.lang.reflect.Type

@JsonAdapter(ITradingAlgorithm.Adapter::class)
sealed interface ITradingAlgorithm {
    //===========================================================//
    //===========================================================//
    // Public Method(es)

    /**
     * Executes the algorithm based on current holdings and market conditions.
     *
     * @param holdings the list of currently owned market.
     * @param allocatedCapital the amount of capital allocated for trading.
     * @param currentPrice the current market price of the asset.
     * @return contains the decision/results.
     */
    fun run(holdings: List<SecurityHolding>, allocatedCapital: Double, currentPrice: Double): Output

    //===========================================================//
    //===========================================================//
    // Serialization(s)

    class Adapter : JsonSerializer<ITradingAlgorithm>, JsonDeserializer<ITradingAlgorithm> {
        override fun serialize(src: ITradingAlgorithm, typeOfT: Type, context: JsonSerializationContext): JsonElement {
            val jsonElement = context.serialize(src, src.javaClass).asJsonObject

            val typeTag = when (src) {
                is TACPP46 -> "TACPP46"
                is ALGDES2 -> "ALGDES2"
                is ALGDES3 -> "ALGDES3"
                is ALGDES31 -> "ALGDES31"
                is ALGDES4 -> "ALGDES4"
            }
            jsonElement.addProperty("algorithmType", typeTag)

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
                else -> throw JsonParseException("Unknown algorithm type tag: $typeTag")
            }
        }
    }
}