package api.controller

import api.dto.TraderResponse
import api.dto.toResponse
import api.service.IControllerService
import api.service.auth.IAuthenticationService
import data.repository.trader.sql.toDomain
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.*

//===========================================================//
//===========================================================//

@RestController
@RequestMapping("/api/portfolio/traders/")
class TraderController(
    private val authService: IAuthenticationService,
    private val controllerService: IControllerService
) {
    //===========================================================//
    //===========================================================//
    // GET

    @GetMapping
    suspend fun getAllTraders(): ResponseEntity<List<TraderResponse>> {
        val userId = authService.currentUser()
        val tradersResponse = controllerService.getAllTraders(userId)
            .map { it.toResponse() }

        return ResponseEntity.ok(
            tradersResponse
        )
    }

    //===========================================================//

    @GetMapping("{traderId}/")
    suspend fun getTraderById(@PathVariable traderId: UUID): ResponseEntity<TraderResponse> {
        val userId = authService.currentUser()
        val traderResponse = controllerService.getTrader(userId, traderId).toResponse()

        return ResponseEntity.ok(
            traderResponse
        )
    }
}