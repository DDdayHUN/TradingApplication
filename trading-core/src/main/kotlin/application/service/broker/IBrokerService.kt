package application.service.broker

import application.service.user.User
import domain.trader.TradingOrder
import java.util.UUID

interface IBrokerService {
    suspend fun submitOrder(userId: UUID, order: TradingOrder)
    suspend fun getAllOrder(userId: UUID): List<TradingOrder>
    fun getAccountSummary(userId: UUID): User.AccountSummary
}