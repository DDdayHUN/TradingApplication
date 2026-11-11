package application.service.trader

import data.repository.trader.sql.TraderEntity
import domain.algorithm.TradingAlgorithm
import domain.trader.SellSecurityHolding
import domain.trader.Trader
import domain.trader.TradingOrder
import java.util.*

interface ITraderService {
    suspend fun createTrader(portfolioId: UUID, trader: Trader): TraderEntity
    suspend fun updateTrader(trader: Trader): TraderEntity
    suspend fun deleteTrader(traderId: UUID)

    suspend fun executeAllTrader(userId: UUID): List<TradingOrder>
    // TODO [v2.0]: "This must be replaced in future releases, cuz this doesn't support large amounts of users"
    suspend fun executeAllTrader(): List<TradingOrder>
    suspend fun forceSellAllHolding(userId: UUID, traderId: UUID): TradingOrder
    suspend fun forceSellAllHolding(userId: UUID): List<TradingOrder>

    // TODO [v1.0] : Move this into TradingOrderService or a more related service
    @Deprecated("Will be moved to another service in the future")
    suspend fun applyBuyFill(traderId: UUID, filledQuantity: Int, averageFillPrice: Double)
    @Deprecated("Will be moved to another service in the future")
    suspend fun applySellFill(traderId: UUID, sellAllocations: Set<SellSecurityHolding>, averageFillPrice: Double)
}