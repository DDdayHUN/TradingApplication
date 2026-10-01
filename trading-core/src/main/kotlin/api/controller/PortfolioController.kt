package api.controller

import api.dto.PortfolioResponse
import api.dto.toResponse
import application.service.auth.IAuthenticationService
import application.service.portfolio.IPortfolioService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.*

//===========================================================//
//===========================================================//

@RestController
@RequestMapping("/api/portfolio/")
class PortfolioController(
    private val portfolioService: IPortfolioService,
    private val authService: IAuthenticationService
) {
    //===========================================================//
    //===========================================================//
    // GET

    //===========================================================//

    @GetMapping
    suspend fun getPortfolio(): ResponseEntity<PortfolioResponse> {
        val userId = authService.currentUser().id
        val summary = portfolioService.getPortfolioAccountSummary(userId)
        val response = portfolioService.getPortfolioByUserId(userId).toResponse(
            availableCapital = summary.availableCapital,
            liquidation = summary.netLiquidation
        )

        return ResponseEntity.ok(response)
    }

    //===========================================================//
    //===========================================================//
    // POST

    @PostMapping
    suspend fun createPortfolio(): ResponseEntity<PortfolioResponse> {
        val userId = authService.currentUser().id
        val portfolio = portfolioService.createPortfolio(userId)
        val summary = portfolioService.getPortfolioAccountSummary(userId)
        val response = portfolio.toResponse(
            availableCapital = summary.availableCapital,
            liquidation = summary.netLiquidation
        )

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(response)
    }
}