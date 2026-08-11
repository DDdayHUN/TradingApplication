package application.service

import api.dto.portfolio.PortfolioResponse
import api.exception.portfolio.PortfolioNotFoundException
import data.persistence.mapper.PortfolioMapper
import data.persistence.repository.IPortfolioRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class PortfolioService {

    //===========================================================//
    //===========================================================//
    // Private Field(s)

    private val portfolioRepository: data.persistence.repository.IPortfolioRepository
    private val portfolioMapper: data.persistence.mapper.PortfolioMapper

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
    // Constructor(s)

    constructor(portfolioRepository: IPortfolioRepository, portfolioMapper: PortfolioMapper) {
        this.portfolioRepository = portfolioRepository
        this.portfolioMapper = portfolioMapper
    }
}