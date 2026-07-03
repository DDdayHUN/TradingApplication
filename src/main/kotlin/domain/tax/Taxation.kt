package domain.tax

object Taxation {
    fun get(type: Type): ITaxation {
        return when (type) {
            is Type.Hungary -> HungaryTaxation
        }
    }

    sealed interface Type {
        data object Hungary : Type
    }
}