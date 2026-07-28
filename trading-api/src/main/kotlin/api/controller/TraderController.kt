package api.controller

import api.dto.trader.CreateTraderRequest
import api.dto.trader.TraderResponse
import api.service.TraderService
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/traders")
class TraderController {

    private val traderService: TraderService

    @PostMapping
    fun createTrader(
        @AuthenticationPrincipal jwt: Jwt,
        @RequestBody request: CreateTraderRequest
    ): ResponseEntity<TraderResponse>{
        val response = traderService.createTrader(
            keycloakSub = jwt.subject,


        )
    }



    constructor(traderService: TraderService) {
        this.traderService = traderService
    }
}