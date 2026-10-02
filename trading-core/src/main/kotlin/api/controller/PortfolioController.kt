package api.controller

import api.dto.PortfolioResponse
import api.dto.toResponse
import api.service.auth.IAuthenticationService
import application.service.portfolio.IPortfolioService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

//===========================================================//
//===========================================================//

@RestController
@RequestMapping("/api/portfolio/")
class PortfolioController(
    private val authService: IAuthenticationService,
    private val portfolioService: IPortfolioService
) {
    //===========================================================//
    //===========================================================//
    // GET

    @GetMapping
    suspend fun getPortfolio(): ResponseEntity<PortfolioResponse> {
        return ResponseEntity.ok(
            portfolioService.get(authService.currentUser()).toResponse()
        )
    }

    //===========================================================//
    //===========================================================//
    // POST

    @PostMapping
    suspend fun createPortfolio(): ResponseEntity<PortfolioResponse> {
        val userId = authService.currentUser()
        val portfolio = portfolioService.create(userId)
        val response = portfolio.toResponse()

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(response)
    }
}