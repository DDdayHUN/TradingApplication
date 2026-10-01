package api.controller

import api.dto.PortfolioResponse
import api.dto.toResponse
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
) {
    //===========================================================//
    //===========================================================//
    // GET

    @GetMapping
    suspend fun getAllPortfolio(): ResponseEntity<List<PortfolioResponse>> {
        val response = portfolioService.getAll().map { portfolio ->
            portfolio.toResponse()
        }

        return ResponseEntity.ok(
            response
        )
    }

    //===========================================================//

    @GetMapping("/{portfolioId}")
    suspend fun getPortfolioById(@PathVariable portfolioId: UUID): ResponseEntity<PortfolioResponse> {
        val portfolio = portfolioService.getById(portfolioId)

        val response = portfolio.toResponse()

        return ResponseEntity.ok(
            response
        )
    }

    //===========================================================//
    //===========================================================//
    // POST

    @PostMapping
    suspend fun createPortfolio(): ResponseEntity<PortfolioResponse> {
        val portfolio = portfolioService.create()
        val response = portfolio.toResponse()

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(response)
    }
}