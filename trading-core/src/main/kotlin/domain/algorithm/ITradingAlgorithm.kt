package domain.algorithm

import com.google.gson.annotations.JsonAdapter
import domain.adapter.AlgorithmAdapter
import domain.algorithm.TradingAlgorithm.Output
import domain.market.security.SecurityHolding

@JsonAdapter(AlgorithmAdapter::class)
sealed interface ITradingAlgorithm {
    //===========================================================//
    //===========================================================//
    // Public Method(es)

    /**
     * Executes the algorithm based on current holdings and market conditions.
     *
     * @param holdings the set of currently owned market assets.
     * @param allocatedCapital the amount of capital allocated for trading.
     * @param currentPrice the current market price of the asset.
     * @return contains the decision/results.
     */
    fun run(holdings: Set<SecurityHolding>, allocatedCapital: Double, currentPrice: Double): Output

    companion object {
        private data class AlgorithmRegistryEntry(
            val tag: String,
            val type: TradingAlgorithm.Type,
            val clazz: Class<out ITradingAlgorithm>
        )

        private val sp_REGISTRY = listOf(
            AlgorithmRegistryEntry("TACPP46",    TradingAlgorithm.Type.TACPP46,    TACPP46::class.java),
            AlgorithmRegistryEntry("ALGDES2",    TradingAlgorithm.Type.ALGDES2,    ALGDES2::class.java),
            AlgorithmRegistryEntry("ALGDES3",    TradingAlgorithm.Type.ALGDES3,    ALGDES3::class.java),
            AlgorithmRegistryEntry("ALGDES31",   TradingAlgorithm.Type.ALGDES31,   ALGDES31::class.java),
            AlgorithmRegistryEntry("ALGDES4",    TradingAlgorithm.Type.ALGDES4,    ALGDES4::class.java),
            AlgorithmRegistryEntry("BUYANDHOLD", TradingAlgorithm.Type.BUYANDHOLD, BUYANDHOLD::class.java),
            AlgorithmRegistryEntry("TACPP462",   TradingAlgorithm.Type.TACPP462,   TACPP462::class.java),
        )

        private val sp_TagToType  = sp_REGISTRY.associate { it.tag to it.type }
        //private val sp_TypeToTag  = sp_REGISTRY.associate { it.type to it.tag }
        private val sp_TagToClass = sp_REGISTRY.associate { it.tag to it.clazz }
        private val sp_ClassToTag = sp_REGISTRY.associate { it.clazz to it.tag }

        fun typeFromTag(tag: String): Result<TradingAlgorithm.Type> {
            return runCatching {
                sp_TagToType[tag]
                    ?: throw IllegalArgumentException("Unknown Trading algorithm type with tag: $tag")
            }
        }

        fun classFromTag(tag: String): Result<Class<out ITradingAlgorithm>> {
            return runCatching {
                sp_TagToClass[tag]
                    ?: throw IllegalArgumentException("Unknown Trading algorithm type with tag: $tag")
            }
        }

        fun typeTagOf(algorithm: ITradingAlgorithm): Result<String> {
            return runCatching {
                sp_ClassToTag[algorithm::class.java]
                    ?: throw IllegalArgumentException("Unknown Trading algorithm type: $algorithm")
            }
        }
    }
}