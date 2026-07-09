package domain.tax

@Deprecated("WARNING : Taxation has threading problems and implementation hiccups, thus it is not representative of truly real world taxation especially over the long run.")
object Taxation {
    fun create(type: Type): ITaxation {
        return when (type) {
            is Type.Hungary -> HungaryTaxation()
        }
    }

    sealed interface Type {
        data object Hungary : Type

        companion object{
            val entries: List<Type> = listOf(
                Hungary
            )
        }
    }
}