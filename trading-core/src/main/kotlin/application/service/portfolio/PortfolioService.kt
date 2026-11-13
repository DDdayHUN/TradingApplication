package application.service.portfolio

import application.service.trader.ITraderService
import application.service.user.IUserService
import data.repository.portfolio.IPortfolioRepository
import data.repository.portfolio.sql.PortfolioEntity
import data.repository.portfolio.sql.toDomain
import data.repository.portfolio.sql.toEntity
import data.repository.trader.sql.toDomain
import data.repository.user.sql.toDomain
import data.repository.user.sql.toEntity
import domain.Portfolio
import exception.TraderHoldingsNotEmptyException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
class PortfolioService(
    private val portfolioRepository: IPortfolioRepository,
    private val traderService: ITraderService,
) : IPortfolioService {
    //===========================================================//
    //===========================================================//
    // Public Method(s)

    @Transactional
    override suspend fun updatePortfolio(portfolio: Portfolio): PortfolioEntity {
        val portfolioEntity = portfolioRepository.getById(portfolio.id).getOrThrow()
        val domainTradersById = portfolio.traders.associateBy { it.id }
        val entityTradersById = portfolioEntity.traders.associateBy { it.id }

        // remove traders that no longer exist
        portfolioEntity.traders
            .filter { it.id !in domainTradersById }
            .forEach { traderEntity ->
                traderService.deleteTrader( traderEntity.id)
            }

        // add new traders
        portfolio.traders
            .filter { it.id !in entityTradersById }
            .forEach { trader ->
                traderService.createTrader(portfolioEntity.id, trader)
            }

        // update existing traders
        portfolioEntity.traders
            .forEach { existing ->
                domainTradersById[existing.id]?.let { domainTrader ->
                    traderService.updateTrader(domainTrader)
                }
            }

        return portfolioRepository.save(portfolioEntity).getOrThrow()
    }
}