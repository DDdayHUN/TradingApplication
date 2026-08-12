package application.service

import api.dto.CreatePortfolioRequest
import api.dto.PortfolioResponse
import api.dto.toResponse
import domain.Portfolio
import domain.interfaces.IPortfolioRepository
import domain.interfaces.IUserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class PortfolioService {

    //===========================================================//
    //===========================================================//
    // Private Field(s)

    private val portfolioRepository: IPortfolioRepository
    private val userRepository: IUserRepository

    //===========================================================//
    //===========================================================//
    // Public Method(s)

    @Transactional
    suspend fun createPortfolio(userId: UUID, request: CreatePortfolioRequest): PortfolioResponse {
        require(request.capital >= 0.0) {
            "Portfolio capital must be greater or equal to zero"
        }

        val user = userRepository.getById(userId).getOrThrow()
        val portfolio = Portfolio(
            userId = user.id,
            capital = request.capital
        )

        portfolioRepository.save(portfolio).getOrThrow()

        return portfolio.toResponse()
    }

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

    constructor(portfolioRepository: IPortfolioRepository, userRepository: IUserRepository) {
        this.portfolioRepository = portfolioRepository
        this.userRepository = userRepository
    }
}