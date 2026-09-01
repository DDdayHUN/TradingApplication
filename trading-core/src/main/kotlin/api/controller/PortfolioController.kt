package api.controller

import api.dto.PortfolioResponse
import api.dto.toResponse
import application.service.IAuthenticationService
import application.service.IPortfolioService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.*

//===========================================================//
//===========================================================//

@RestController
@RequestMapping("/api/portfolio")
class PortfolioController(
    private val portfolioService: IPortfolioService,
    private val session: IAuthenticationService
) {
    //===========================================================//
    //===========================================================//
    // GET

    @GetMapping
    suspend fun getAllPortfolio(): ResponseEntity<List<PortfolioResponse>> {
        val userId = session.currentUser().id
        val response = portfolioService.getAllPortfolio(userId).map { portfolio ->
            portfolio.toResponse(portfolioService.getAvailableCapital(portfolio.id))
        }

        return ResponseEntity.ok(
            response
        )
    }

    //===========================================================//

    @GetMapping("/{portfolioId}")
    suspend fun getPortfolioById(@PathVariable portfolioId: UUID): ResponseEntity<PortfolioResponse> {
        val response = portfolioService.getPortfolio(portfolioId).toResponse(portfolioService.getAvailableCapital(portfolioId))
        return ResponseEntity.ok(response)
    }

    //===========================================================//
    //===========================================================//
    // POST

    @PostMapping
    suspend fun createPortfolio(): ResponseEntity<PortfolioResponse> {
        val userId = session.currentUser().id
        val portfolio = portfolioService.createPortfolio(userId)
        val response = portfolio.toResponse(portfolioService.getAvailableCapital(portfolio.id))


        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(response)
    }
}