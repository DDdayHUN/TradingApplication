package application.service.spring

import application.service.IPortfolioService
import application.service.broker.IBrokerService
import domain.Portfolio
import domain.interfaces.IPortfolioRepository
import infrastructure.broker.IbkrSession
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

//===========================================================//
//===========================================================//

@Service
class PortfolioService(
    private val portfolioRepository: IPortfolioRepository,
    private val ibkrService: IBrokerService
) : IPortfolioService {
    //===========================================================//
    //===========================================================//
    // Public Method(s)

    @Transactional
    override suspend fun save(portfolio: Portfolio): Portfolio {
        return portfolioRepository.save(portfolio).getOrThrow()
    }

    //===========================================================//

    @Transactional
    override suspend fun createPortfolio(userId: UUID): Portfolio {
        val portfolio = Portfolio()
        return portfolioRepository.create(userId, portfolio).getOrThrow()
    }

    //===========================================================//

    @Transactional(readOnly = true)
    override suspend fun getAllPortfolio(userId: UUID): List<Portfolio> {
        return portfolioRepository.getAllByUserId(userId).getOrThrow()
    }

    //===========================================================//

    @Transactional(readOnly = true)
    override suspend fun getPortfolio(userId: UUID, id: UUID): Portfolio {
        return  portfolioRepository.getByIdForUser(userId, id).getOrThrow()
    }

    //===========================================================//

    @Transactional(readOnly = true)
    override suspend fun getPortfolio(portfolioId: UUID): Portfolio {
        return portfolioRepository.getById(portfolioId).getOrThrow()
    }

    //===========================================================//

    @Transactional(readOnly = true)
    override suspend fun getPortfolioByTraderId(traderId: UUID): Portfolio {
        return portfolioRepository.getByTraderId(traderId).getOrThrow()
    }

    //===========================================================//

    @Transactional
    override suspend fun deleteAllPortfolio(userId: UUID): Boolean {
        TODO("Not yet implemented")
    }

    //===========================================================//

    @Transactional
    override suspend fun deletePortfolio(userId: UUID, id: UUID): Boolean {
        TODO("Not yet implemented")
    }

    //===========================================================//

    @Transactional(readOnly = true)
    override suspend fun getAvailableCapital(portfolioId: UUID): Double {
        val portfolio = getPortfolio(portfolioId)

        val brokerAvailableCash = ibkrService.getAvailableCapital()

        val traderCapital = portfolio.traders.sumOf{trader->
            trader.capital
        }

        return (brokerAvailableCash - traderCapital).coerceAtLeast(0.0)
    }

    //===========================================================//

    @Transactional(readOnly = true)
    override suspend fun getLiquidation(): Double {
        return ibkrService.getNetLiquidation()
    }
}