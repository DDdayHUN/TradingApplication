package api.controller

import api.dto.CreateTraderRequest
import api.dto.TraderResponse
import api.dto.toResponse
import api.service.auth.IAuthenticationService
import application.service.trader.ITraderService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.*

//===========================================================//
//===========================================================//

@RestController
@RequestMapping("/api/portfolio/traders/")
class TraderController(
    private val authService: IAuthenticationService,
    private val traderService: ITraderService
) {
    //===========================================================//
    //===========================================================//
    // GET

    @GetMapping
    suspend fun getAllTraders(): ResponseEntity<List<TraderResponse>> {
        return ResponseEntity.ok(
            traderService.getAll(
                userId = authService.currentUser()
            ). map {trader -> trader.toResponse()}
        )
    }

    //===========================================================//

    @GetMapping("{traderId}/")
    suspend fun getTraderById(@PathVariable traderId: UUID): ResponseEntity<TraderResponse> {
        return ResponseEntity.ok(
            traderService.getById(
                userId = authService.currentUser(),
                traderId = traderId
            ).toResponse()
        )
    }

    //===========================================================//
    //===========================================================//
    // POST

    @PostMapping
    suspend fun createTrade(@RequestBody request: CreateTraderRequest): ResponseEntity<TraderResponse> {
        return ResponseEntity.ok(
            traderService.createTrader(
                userId = authService.currentUser(),
                request = request
            ).toResponse()
        )
    }
}