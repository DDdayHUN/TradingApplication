package api.controller

import api.dto.PortfolioResponse
import api.dto.toResponse
import api.service.auth.IAuthenticationService
import api.service.portfolio.IPortfolioService
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
        val user = authService.currentUser()
        val response = user.portfolio?.toResponse()

        return ResponseEntity.ok(response)
    }

    //===========================================================//
    //===========================================================//
    // POST

    @PostMapping
    suspend fun createPortfolio(): ResponseEntity<PortfolioResponse> {
        val portfolio = portfolioService.createNewPortfolio()
        val response = portfolio.toResponse()

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(response)
    }
}