package application.handlers

import domain.signal.TradingSignal
import domain.trader.Trader
import service.trader.ITradingService

//===========================================================//
/**
 * Handler which calls service to buy or sell security.
 * This handler needs to be registered to a dispatcher
 *
 * @property m_TradingService the service which executes buy and sell
 */
//===========================================================//
class TradingOrderHandler : ITradingHandler {

    //===========================================================//
    //===========================================================//
    // Public Fields(es)

    private val m_TradingService : ITradingService

    //===========================================================//
    //===========================================================//
    // Public Methods(es)

    override suspend fun handle(trader: Trader, signal: TradingSignal){
        if(signal.sell != null){
            onSell(trader, signal)
            return
        }
        if(signal.buy != null) {
            onBuy(trader, signal)
            return
        }
    }

    //===========================================================//
    //===========================================================//
    // Private Methods(es)

    private suspend fun onSell(trader: Trader, signal: TradingSignal) {
        val sell = signal.sell ?: return

        val ret = m_TradingService.executeSellAsync(
            trader.securityIdentifier,
            sell,
            signal.currentPrice
        )

        if(ret.success) {
            trader.finalizeOrder(signal.sell, signal.currentPrice)
        }else {
            return
        }

    }

    private suspend fun onBuy(trader: Trader, signal: TradingSignal) {
        val buy = signal.buy?: return

        val ret = m_TradingService.executeBuyAsync(
            trader.securityIdentifier,
            buy,
            signal.currentPrice
        )

        if(ret.success) {
            trader.finalizeOrder(signal.buy, signal.currentPrice)
        }else{
            return
        }
    }

    //===========================================================//
    //===========================================================//
    // Constructor(s)

    constructor(tradingService: ITradingService) {
        m_TradingService = tradingService
    }
}