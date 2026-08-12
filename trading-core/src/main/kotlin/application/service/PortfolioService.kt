package application.service

import api.dto.PortfolioResponse
import api.dto.toResponse
import exception.api.PortfolioNotFoundException
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

    //===========================================================//
    //===========================================================//
    // Public Method(s)

    @Transactional(readOnly = true)
    suspend fun getAllByUserId(userId: UUID): List<PortfolioResponse> {
        val portfolioList = portfolioRepository.getAllByUserId(userId).getOrThrow()

        return portfolioList.map  { portfolio ->
            portfolio.toResponse()
        }
    }

    //===========================================================//

    @Transactional(readOnly = true)
    suspend fun findById(id: UUID): PortfolioResponse {
         return portfolioRepository.getById(id)
            .getOrThrow()
            .toResponse()
    }

    //===========================================================//
    //===========================================================//
    // Constructor(s)

    constructor(portfolioRepository: IPortfolioRepository) {
        this.portfolioRepository = portfolioRepository
    }
}