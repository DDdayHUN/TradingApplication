package application.service

import api.dto.PortfolioResponse
import exception.api.PortfolioNotFoundException
import data.repository.portfolio.PortfolioMapper
import data.repository.portfolio.IPortfolioJpaRepository
import data.repository.portfolio.PortfolioEntity
import domain.interfaces.IPortfolioRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class PortfolioService {

    //===========================================================//
    //===========================================================//
    // Private Field(s)

    private val portfolioRepository: IPortfolioRepository
    private val portfolioMapper: PortfolioMapper

    //===========================================================//
    //===========================================================//
    // Public Method(s)

    @Transactional(readOnly = true)
    fun findForCurrentUser(keycloakSub: String): PortfolioResponse {
        val portfolio = portfolioRepository.findByUserKeycloakSub(keycloakSub)
            ?: throw PortfolioNotFoundException(keycloakSub)

        return portfolioMapper.toResponse(portfolio)
    }

    //===========================================================//

    @Transactional(readOnly = true)
    fun findById(id: UUID): PortfolioResponse {
        val portfolio = portfolioRepository.findById(id)
            .orElseThrow {
                PortfolioNotFoundException(id)
            }

        return portfolioMapper.toResponse(portfolio)
    }

    //===========================================================//
    //===========================================================//
    // Helper Method(es)

    fun toResponse(portfolio: PortfolioEntity): PortfolioResponse {
        return PortfolioResponse(

        )
    }


    //===========================================================//
    //===========================================================//
    // Constructor(s)

    constructor(portfolioRepository: IPortfolioRepository, portfolioMapper: PortfolioMapper) {
        this.portfolioRepository = portfolioRepository
        this.portfolioMapper = portfolioMapper
    }
}