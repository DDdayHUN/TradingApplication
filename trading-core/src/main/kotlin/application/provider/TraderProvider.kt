package application.provider

import data.repository.trader.FakeTraderRepository
import domain.interfaces.ITraderRepository

@Deprecated(message = "Is superseded by IPortfolioRepository")
object TraderProvider {
    fun get(type: Type): ITraderRepository {
        return when (type) {
            is Type.Fake -> FakeTraderRepository
        }
    }

    sealed interface Type {
        data object Fake : Type
    }
}