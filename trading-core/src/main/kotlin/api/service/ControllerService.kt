package api.service

import application.service.user.User
import data.repository.user.IUserRepository
import domain.Portfolio
import domain.trader.Trader
import java.util.UUID


class ControllerService(
    private val userRepository: IUserRepository
) : IControllerService {
    override suspend fun getUser(userId: UUID): User {
        TODO("Not yet implemented")
    }

    override suspend fun getPortfolio(userId: UUID): Portfolio {
        TODO("Not yet implemented")
    }

    override suspend fun getAllTraders(userId: UUID): List<Trader> {
        TODO("Not yet implemented")
    }

    override suspend fun getTrader(userId: UUID, traderId: UUID): Trader {
        TODO("Not yet implemented")
    }

}