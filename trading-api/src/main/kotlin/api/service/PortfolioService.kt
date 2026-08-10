package api.service

import api.dto.portfolio.PortfolioResponse
import api.exception.portfolio.PortfolioNotFoundException
import api.mapper.PortfolioMapper
import api.repository.IPortfolioRepository
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
    // Constructor(s)

    constructor(portfolioRepository: IPortfolioRepository, portfolioMapper: PortfolioMapper) {
        this.portfolioRepository = portfolioRepository
        this.portfolioMapper = portfolioMapper
    }
}