package api.controller

import api.dto.PortfolioResponse
import api.dto.toResponse
import application.service.IPortfolioService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

//===========================================================//
//===========================================================//

@RestController
@RequestMapping("/api/portfolio")
class PortfolioController(
    private val portfolioService: IPortfolioService
) {
    //===========================================================//
    //===========================================================//
    // GET

    @GetMapping
    suspend fun getAllPortfolio(): ResponseEntity<List<PortfolioResponse>> {
        val response = portfolioService.getAllPortfolio().map { portfolio -> portfolio.toResponse() }

        return ResponseEntity.ok(
            response
        )
    }

    //===========================================================//

    @GetMapping("/{portfolioId}")
    suspend fun getPortfolioById(@PathVariable portfolioId: UUID): ResponseEntity<PortfolioResponse> {
        val response = portfolioService.getPortfolio(portfolioId).toResponse()
        return ResponseEntity.ok(response)
    }

    //===========================================================//
    //===========================================================//
    // POST

    @PostMapping
    suspend fun createPortfolio(): ResponseEntity<PortfolioResponse> {
        val response = portfolioService.createPortfolio().toResponse()

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(response)
    }
}