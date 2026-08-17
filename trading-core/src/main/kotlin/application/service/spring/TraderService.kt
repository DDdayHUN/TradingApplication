package application.service.spring

import api.dto.ChangeTraderAlgorithmRequest
import api.dto.CreateTraderRequest
import api.dto.TraderResponse
import api.dto.toResponse
import application.service.IPortfolioService
import application.service.ITraderService
import data.network.MarketDataProvider
import domain.Portfolio
import domain.algorithm.TradingAlgorithm
import domain.market.security.SecurityIdentifier
import domain.trader.Trader
import domain.trader.TradingOrder
import exception.api.PortfolioNotFoundException
import exception.api.TraderNotFoundException
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

//===========================================================//
//===========================================================//

class TraderService(
    private val portfolio: IPortfolioService
) : ITraderService {
    //===========================================================//
    //===========================================================//
    // Public Method(s)

    @Transactional
    suspend fun createTrader(portfolioId: UUID, capital: Double): Trader {
        TODO("Implement later")
    }

    //===========================================================//

    @Transactional(readOnly = true)
    suspend fun getAllByPortfolioId(portfolioId: UUID): List<TraderResponse> {
        TODO("Implement later")
    }

    //===========================================================//

    @Transactional(readOnly = true)
    suspend fun getById(userId: UUID, portfolioId: UUID, traderId: UUID): TraderResponse {
        TODO("Implement later")
    }

    //===========================================================//

    @Transactional
    suspend fun changeAlgorithm(traderId: UUID, userId: UUID,portfolioId: UUID, request: ChangeTraderAlgorithmRequest): TraderResponse {
        TODO("Implement later")
    }

    //===========================================================//

    @Transactional
    suspend fun executeTrader(userId: UUID, portfolioId: UUID, traderId: UUID): TradingOrder {
        TODO("Implement later")
    }
}