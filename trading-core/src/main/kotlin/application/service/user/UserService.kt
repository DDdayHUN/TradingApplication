package application.service.user

import application.service.trader.TraderService
import data.repository.user.IUserRepository
import data.repository.user.sql.toDomain
import data.repository.user.sql.update
import domain.trader.TradingOrder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
class UserService(
    private val traderService: TraderService,
    private val userRepository: IUserRepository
): IUserService {
    //===========================================================//
    //===========================================================//
    // Public Method(s)

    @Transactional
    override suspend fun update(user: User): User {
        val userEntity = userRepository.getById(user.id).getOrThrow()

        userEntity.update(user)
        return userEntity.toDomain()
    }

    //===========================================================//

    @Transactional(readOnly = true)
    override suspend fun getById(userId: UUID): User {
        return userRepository.getById(userId).getOrThrow().toDomain()
    }

    //===========================================================//

    @Transactional(readOnly = true)
    override suspend fun getAll(): List<User> {
        return userRepository.getAll().getOrThrow().map { it.toDomain() }
    }

    //===========================================================//

    @Transactional
    override suspend fun executeTradeForAll(): List<TradingOrder> {
        val users = getAll()

        return users.flatMap { user ->
            traderService.executeAllTrader(user.id)
        }
    }
}