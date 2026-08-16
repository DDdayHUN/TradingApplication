package api.controller

import api.dto.CreatePortfolioRequest
import api.dto.PortfolioResponse
import application.service.IAuthenticationService
import application.service.PortfolioService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/portfolio")
class NewPortfolioController(
    private val auth: IAuthenticationService,
    private val portfolioService: PortfolioService
) {
    //===========================================================//
    //===========================================================//
    // GET

    @GetMapping
    suspend fun getAllPortfolio(): ResponseEntity<List<PortfolioResponse>> {
        val user = auth.currentUser()
        return ResponseEntity.ok(
            portfolioService.getAllByUserId(user.id)
        )
    }

    //===========================================================//
    //===========================================================//
    // POST

    @PostMapping
    suspend fun createPortfolio(
        @RequestBody request: CreatePortfolioRequest
    ): ResponseEntity<PortfolioResponse> {
        val user = auth.currentUser()
        return ResponseEntity.status(HttpStatus.CREATED).body(
            portfolioService.createPortfolio(
                userId = user.id,
                request = request
            )
        )
    }
}