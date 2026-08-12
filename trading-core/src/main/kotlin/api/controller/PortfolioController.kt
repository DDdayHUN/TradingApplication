package api.controller

import api.dto.PortfolioResponse
import application.service.PortfolioService
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/portfolio")
class PortfolioController {

    //===========================================================//
    //===========================================================//
    // Private Field(s)

    private val portfolioService: PortfolioService

    //===========================================================//
    //===========================================================//
    // GET

    @GetMapping
    suspend fun getAllPortfolioByUserId(authentication: Authentication): ResponseEntity<List<PortfolioResponse>>{
        val userId = UUID.fromString(authentication.name)

        return ResponseEntity.ok(
            portfolioService.getAllByUserId(
                userId = userId
            )
        )
    }

    //===========================================================//
    //===========================================================//
    // Constructor(s)

    constructor(portfolioService: PortfolioService){
        this.portfolioService = portfolioService
    }
}