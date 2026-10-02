package application.service.trader

import domain.trader.SellHolding
import domain.trader.TradingOrder
import java.util.UUID

interface ITraderService {
    suspend fun executeTrader(traderId: UUID): TradingOrder?
    suspend fun applyBuyFill(traderId: UUID, filledQuantity: Int, averageFillPrice: Double)
    suspend fun applySellFill(traderId: UUID, sellAllocations: Set<SellHolding>, averageFillPrice: Double)
    suspend fun forceSellHolding(traderId: UUID, securityHoldingId: UUID): TradingOrder
    suspend fun forceSellAllHolding(traderId: UUID): List<TradingOrder>
}