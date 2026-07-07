package data.repository.trader

import domain.interfaces.ITraderRepository

object TraderRepositoryProvider {
    fun get(type: Type): ITraderRepository {
        return when (type) {
            is Type.Fake -> FakeTraderRepository
        }
    }

    sealed interface Type {
        data object Fake : Type
    }
}