package domain.signal

import application.handlers.ITradingHandler
import domain.trader.Trader
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.supervisorScope

//===========================================================//

//===========================================================//
class TradingDispatcher {

    private val listeners =  mutableListOf<ITradingHandler>()

    fun register(listener: ITradingHandler){
        listeners.add(listener)
    }

    fun unregister(listener: ITradingHandler){
        listeners.remove(listener)
    }

    suspend fun dispatch(trader: Trader, signal: TradingSignal) = supervisorScope{
        listeners.map { listener ->
            async {
                listener.handle(trader, signal)
            }
        }.awaitAll()
    }

}