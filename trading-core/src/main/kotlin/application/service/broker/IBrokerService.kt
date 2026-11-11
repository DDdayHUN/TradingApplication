package application.service.broker

import application.service.user.User
import domain.trader.TradingOrder
import java.util.UUID

interface IBrokerService {
    suspend fun submitOrderForUser(userId: UUID, order: TradingOrder)
    suspend fun getAllOrderForUser(userId: UUID): List<TradingOrder>
    fun getAccountSummary(userId: UUID): User.AccountSummary
}