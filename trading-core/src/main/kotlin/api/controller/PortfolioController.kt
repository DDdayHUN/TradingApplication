package api.controller

import api.dto.portfolio.PortfolioResponse
import application.service.PortfolioService
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

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
    fun getCurrentPortfolio(authentication: Authentication): ResponseEntity<PortfolioResponse>{
        return ResponseEntity.ok(
            portfolioService.findForCurrentUser(
                authentication.name
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