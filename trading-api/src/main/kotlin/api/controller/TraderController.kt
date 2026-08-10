package api.controller

import api.dto.trader.ChangeTraderAlgorithmRequest
import api.dto.trader.CreateTraderRequest
import api.dto.trader.TraderResponse
import api.service.TraderService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
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

    @GetMapping("/me")
    fun getAllTradersByKeycloakSub(authentication: Authentication): ResponseEntity<List<TraderResponse>> {
        return ResponseEntity.ok(
            traderService.findAllForUserByKeycloakSub(authentication.name)
        )
    }

    @GetMapping("/{id}")
    fun getTraderById(authentication: Authentication, @PathVariable id: UUID): ResponseEntity<TraderResponse>{
        return ResponseEntity.ok(
            traderService.findByIdForUser(
                id = id,
                keycloakSub = authentication.name
            )
        )
    }

    //===========================================================//
    //===========================================================//
    // POST

    @PostMapping
    fun createTrader(authentication: Authentication, @RequestBody request: CreateTraderRequest
    ): ResponseEntity<TraderResponse>{
        val response = traderService.createTrader(
            keycloakSub = authentication.name,
            request = request
        )

        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }

    //===========================================================//
    //===========================================================//
    // PATCH

    @PatchMapping("/{id}/algorithm")
    fun changeAlgorithm(authentication: Authentication, @PathVariable id: UUID,
                        @RequestBody request: ChangeTraderAlgorithmRequest): ResponseEntity<TraderResponse> {
        return ResponseEntity.ok(
            traderService.changeAlgorithm(
                id = id,
                keycloakSub = authentication.name,
                request = request
            )
        )
    }

    //===========================================================//
    //===========================================================//
    // Constructor(s)

    constructor(traderService: TraderService) {
        this.traderService = traderService
    }
}