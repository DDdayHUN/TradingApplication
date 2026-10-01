package api.controller

import api.dto.CreateTraderRequest
import api.dto.TraderResponse
import api.dto.toResponse
import application.service.auth.IAuthenticationService
import application.service.trader.ITraderService
import exception.api.TraderNotFoundException
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.*

//===========================================================//
//===========================================================//

@RestController
@RequestMapping("/api/portfolio/traders/")
class TraderController(
    private val traderService: ITraderService
) {
    //===========================================================//
    //===========================================================//
    // GET
    @GetMapping
    suspend fun getAllTraders(): ResponseEntity<List<TraderResponse>> {
        val response = traderService.getAll().map { trader ->
            trader.toResponse()
        }
        return ResponseEntity.ok(response)
    }

    //===========================================================//

    @GetMapping("{traderId}")
    suspend fun getTraderById(@PathVariable traderId: UUID): ResponseEntity<TraderResponse> {
        val response = traderService.getById(traderId).toResponse()

        return ResponseEntity.ok(response)
    }

    //===========================================================//
    //===========================================================//
    // POST
    @PostMapping
    suspend fun createTrader(@RequestBody request: CreateTraderRequest): ResponseEntity<TraderResponse> {

        val response = traderService.createTrader(request).toResponse()

        return ResponseEntity.ok(response)
    }

}