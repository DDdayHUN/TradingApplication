package api.controller

import api.dto.CreateTraderRequest
import api.dto.TraderResponse
import api.dto.toResponse
import api.service.auth.IAuthenticationService
import application.service.trader.ITraderService
import application.service.user.IUserService
import data.repository.trader.sql.toDomain
import domain.algorithm.TradingAlgorithm
import domain.market.security.SecurityIdentifier
import domain.trader.Trader
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.*

//===========================================================//
//===========================================================//

@RestController
@RequestMapping("/api/portfolio/traders/")
class TraderController(
    private val authService: IAuthenticationService,
    private val userService: IUserService,
    private val traderService: ITraderService,
) {
    //===========================================================//
    //===========================================================//
    // GET

    @GetMapping
    suspend fun getAllTraders(): ResponseEntity<List<TraderResponse>> {
        val user = userService.getUserById(authService.currentUser())
        val tradersResponse = user.portfolio.traders.map { it.toDomain().toResponse() }

        return ResponseEntity.ok(
            tradersResponse
        )
    }

    //===========================================================//

    @GetMapping("{traderId}/")
    suspend fun getTraderById(@PathVariable traderId: UUID): ResponseEntity<TraderResponse> {
        val user = userService.getUserById(authService.currentUser())
        val traderResponse = user.portfolio.traders.find { it.id == traderId }!!.toDomain().toResponse()

        return ResponseEntity.ok(
            traderResponse
        )
    }

    //===========================================================//
    //===========================================================//
    // POST

    // TODO [v1.0]: Redo this function and the request, because currently we allow the user to set any capital.
    @PostMapping
    suspend fun createTrade(@RequestBody request: CreateTraderRequest): ResponseEntity<TraderResponse> {
        TODO("Awaiting reimplementation")
        /*
        val securityIdentifier = SecurityIdentifier(
            request.securityIdentifier.isin,
            request.securityIdentifier.tickerSymbol,
            request.securityIdentifier.currency
        )
        val algorithmType = TradingAlgorithm.typeFromTag(request.algorithmType).getOrThrow()

        val user = userService.getUserById(authService.currentUser())
        val traderResponse = user.portfolio.traders.find { it.id == traderId }!!.toDomain().toResponse()
        val trader = Trader(
            securityIdentifier = securityIdentifier,
            algorithm = TradingAlgorithm.create()
        )

        val createdTrader = traderService.createTrader(user.portfolio.id, trader).toDomain()

        return ResponseEntity.ok(
            createdTrader.toResponse()
        )*/
    }
}