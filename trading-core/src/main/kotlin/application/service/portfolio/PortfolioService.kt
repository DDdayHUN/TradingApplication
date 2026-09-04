package application.service.portfolio

import application.service.broker.IBrokerService
import domain.Portfolio
import data.repository.portfolio.IPortfolioRepository
import infrastructure.broker.IbkrAccountSummary
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

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
    override suspend fun getAccountSummary(portfolioId: UUID): IbkrAccountSummary {
        val portfolio = getPortfolio(portfolioId)

        val summary = ibkrService.getAccountSummary()

        val traderCapital = portfolio.traders.sumOf{trader->
            trader.capital
        }

       return IbkrAccountSummary(
           availableCapital = (summary.availableCapital - traderCapital).coerceAtLeast(0.0),
           netLiquidation = summary.netLiquidation,
       )
    }
}