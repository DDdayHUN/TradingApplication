package data.service

import domain.interfaces.ITradingService

object TradingServiceProvider {
    fun get(type: Type): ITradingService {
        return when (type) {
            is Type.Fake -> FakeTradingService
        }
    }

    sealed interface Type {
        data object Fake : Type
    }
}