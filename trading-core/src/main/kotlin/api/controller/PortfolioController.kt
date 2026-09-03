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
            val summary = portfolioService.getAccountSummary(portfolio.id)
            portfolio.toResponse(
                availableCapital = summary.availableCapital,
                liquidation = summary.netLiquidation
            )
        }

        return ResponseEntity.ok(
            response
        )
    }

    //===========================================================//

    @GetMapping("/{portfolioId}")
    suspend fun getPortfolioById(@PathVariable portfolioId: UUID): ResponseEntity<PortfolioResponse> {
        val summary = portfolioService.getAccountSummary(portfolioId)
        val response = portfolioService.getPortfolio(portfolioId).toResponse(
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
        val userId = session.currentUser().id
        val portfolio = portfolioService.createPortfolio(userId)
        val summary = portfolioService.getAccountSummary(portfolio.id)
        val response = portfolio.toResponse(
            availableCapital = summary.availableCapital,
            liquidation = summary.netLiquidation
        )


        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(response)
    }
}