package application.service.user

import domain.trader.TradingOrder
import java.util.*

interface IUserService {
    suspend fun update(user: User): User
    suspend fun getById(userId: UUID): User

    @Deprecated("This will be replaced in future releases, cuz this doesn't support large amounts of users")
    suspend fun getAll(): List<User>

    @Deprecated("CODE : 1 :: This will be replaced in future iterations, cuz this doesn't support large amounts of users")
    suspend fun executeTradeForAll(): List<TradingOrder>
}