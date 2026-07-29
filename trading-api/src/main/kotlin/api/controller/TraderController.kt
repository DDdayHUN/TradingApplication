package api.controller

import api.dto.trader.CreateTraderRequest
import api.dto.trader.TraderResponse
import api.service.TraderService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/traders")
class TraderController {

    //===========================================================//
    //===========================================================//
    // Private Field(s)

    private val traderService: TraderService

    //===========================================================//
    //===========================================================//
    // GET

    @GetMapping
    fun getAllTraders(@AuthenticationPrincipal jwt: Jwt): ResponseEntity<List<TraderResponse>> {
        return ResponseEntity.ok(
            traderService.findAllForUserByKeycloakSub(jwt.subject!!)
        )
    }

    @GetMapping("/{id}")
    fun getTraderById(@AuthenticationPrincipal jwt: Jwt, @PathVariable id: UUID): ResponseEntity<TraderResponse>{
        return ResponseEntity.ok(
            traderService.findByIdForUser(
                id = id,
                keycloakSub = jwt.subject!!
            )
        )
    }

    //===========================================================//
    //===========================================================//
    // POST

    @PostMapping
    fun createTrader(
        @AuthenticationPrincipal jwt: Jwt,
        @RequestBody request: CreateTraderRequest
    ): ResponseEntity<TraderResponse>{
        val response = traderService.createTrader(
            keycloakSub = jwt.subject!!,
            request = request
        )

        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }

    //===========================================================//
    //===========================================================//
    // Constructor(s)

    constructor(traderService: TraderService) {
        this.traderService = traderService
    }
}