package api.service

import application.service.user.User
import domain.Portfolio
import domain.trader.Trader
import java.util.UUID

interface IControllerService {
    suspend fun getUser(userId: UUID): User
    suspend fun getPortfolio(userId: UUID): Portfolio
    suspend fun getAllTraders(userId: UUID): List<Trader>
    suspend fun getTrader(userId: UUID, traderId: UUID): Trader
}