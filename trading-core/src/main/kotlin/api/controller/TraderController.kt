package api.controller

import api.dto.CreateTraderRequest
import api.dto.TraderResponse
import api.dto.toResponse
import application.service.trader.ITraderService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.*

//===========================================================//
//===========================================================//

@RestController
@RequestMapping("/api/portfolio/{portfolioId}/traders")
class TraderController(
    private val traderService: ITraderService
) {
    //===========================================================//
    //===========================================================//
    // GET
    @GetMapping
    suspend fun getAllTraders(@PathVariable portfolioId: UUID): ResponseEntity<List<TraderResponse>> {
        val response = traderService.getAllByPortfolioId(portfolioId).map { trader ->
            trader.toResponse()
        }
        return ResponseEntity.ok(response)
    }

    //===========================================================//

    @GetMapping("/{traderId}")
    suspend fun getTraderById(@PathVariable traderId: UUID): ResponseEntity<TraderResponse> {
        val response = traderService.getById(traderId).toResponse()

        return ResponseEntity.ok(response)
    }

    //===========================================================//
    //===========================================================//
    // POST
    @PostMapping
    suspend fun createTrader(@PathVariable portfolioId: UUID, @RequestBody request: CreateTraderRequest): ResponseEntity<TraderResponse> {

        val response = traderService.create(portfolioId, request).toResponse()

        return ResponseEntity.ok(response)
    }

}