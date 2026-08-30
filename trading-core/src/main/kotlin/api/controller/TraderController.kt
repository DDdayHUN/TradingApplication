package api.controller

import api.dto.CreateTraderRequest
import api.dto.TraderResponse
import api.dto.toResponse
import application.service.IAuthenticationService
import application.service.ITraderService
import exception.api.TraderNotFoundException
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

//===========================================================//
//===========================================================//

@RestController
@RequestMapping("/api/portfolio/{portfolioId}/traders")
class TraderController(
    private val traderService: ITraderService,
    private val session: IAuthenticationService
) {
    //===========================================================//
    //===========================================================//
    // GET
    @GetMapping
    suspend fun getAllTraders(@PathVariable portfolioId: UUID): ResponseEntity<List<TraderResponse>> {
        val userId = session.currentUser().id
        val response = traderService.getAllByPortfolioId(userId, portfolioId).map { trader ->
            trader.toResponse()
        }
        return ResponseEntity.ok(response)
    }

    //===========================================================//

    @GetMapping("/{traderId}")
    suspend fun getTraderById(@PathVariable traderId: UUID, @PathVariable portfolioId: UUID): ResponseEntity<TraderResponse> {
        val userId = session.currentUser().id
        val response = traderService.getById(userId, portfolioId, traderId)
            ?: throw TraderNotFoundException(traderId)

        return ResponseEntity.ok(response.toResponse())
    }

    //===========================================================//
    //===========================================================//
    // POST
    @PostMapping
    suspend fun createTrader(@PathVariable portfolioId: UUID, @RequestBody request: CreateTraderRequest): ResponseEntity<TraderResponse> {
        val userId= session.currentUser().id
        val response = traderService.createTrader(userId, portfolioId, request).toResponse()

        return ResponseEntity.ok(response)
    }

}