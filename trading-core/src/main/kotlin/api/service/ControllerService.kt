package api.service

import application.service.user.User
import data.repository.portfolio.sql.toDomain
import data.repository.trader.sql.toDomain
import data.repository.user.IUserRepository
import data.repository.user.sql.toDomain
import domain.Portfolio
import domain.trader.Trader
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class ControllerService(
    private val userRepository: IUserRepository
) : IControllerService {
    //===========================================================//
    //===========================================================//
    // Public Method(s)

    @Transactional(readOnly = true)
    override suspend fun getUser(userId: UUID): User {
        val userEntity = userRepository.getById(userId).getOrThrow()
        return userEntity.toDomain()
    }

    //===========================================================//

    @Transactional(readOnly = true)
    override suspend fun getPortfolio(userId: UUID): Portfolio {
        val userEntity = userRepository.getById(userId).getOrThrow()
        return userEntity.portfolio.toDomain()
    }

    //===========================================================//

    @Transactional(readOnly = true)
    override suspend fun getAllTraders(userId: UUID): List<Trader> {
        val userEntity = userRepository.getById(userId).getOrThrow()
        return userEntity.portfolio.traders.map { it.toDomain() }
    }

    //===========================================================//

    @Transactional(readOnly = true)
    override suspend fun getTrader(userId: UUID, traderId: UUID): Trader {
        val userEntity = userRepository.getById(userId).getOrThrow()
        return userEntity.portfolio.traders.first { it.id == traderId }.toDomain()
    }
}