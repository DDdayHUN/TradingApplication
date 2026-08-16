package api.controller

import api.dto.ChangeTraderAlgorithmRequest
import api.dto.CreateTraderRequest
import api.dto.TraderResponse
import application.service.TraderService
import domain.trader.TradingOrder
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

//===========================================================//
//===========================================================//

@RestController
@RequestMapping("/api/portfolios/{portfolioId}/traders")
class TraderController(
    private val traderService: TraderService
) {
    //===========================================================//
    //===========================================================//
    // GET

    @GetMapping
    suspend fun getAll(
        authentication: Authentication,
        @PathVariable portfolioId: UUID
    ): ResponseEntity<List<TraderResponse>>{
        val userId = UUID.fromString(authentication.name)

        return ResponseEntity.ok(
            traderService.getAllByPortfolioId(
                userId = userId,
                portfolioId = portfolioId
            )
        )
    }

    //===========================================================//

    @GetMapping("/{traderId}")
    suspend fun getTraderById(
        authentication: Authentication,
        @PathVariable traderId: UUID,
        @PathVariable portfolioId: UUID
    ): ResponseEntity<TraderResponse>{
        val userId = UUID.fromString(authentication.name)

        return ResponseEntity.ok(
            traderService.getById(
                userId = userId,
                portfolioId = portfolioId,
                traderId = traderId
            )
        )
    }

    //===========================================================//
    //===========================================================//
    // POST

    @PostMapping
    suspend fun createTrader(
        authentication: Authentication,
        @PathVariable portfolioId: UUID,
        @RequestBody request: CreateTraderRequest
    ): ResponseEntity<TraderResponse>{
        val userId = UUID.fromString(authentication.name)

        val response = traderService.createTrader(
            userId = userId,
            portfolioId = portfolioId,
            request = request
        )

        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }

    //===========================================================//

    @PostMapping("/{traderId}/execute")
    suspend fun executeTrader(
        authentication: Authentication,
        @PathVariable portfolioId: UUID,
        @PathVariable traderId: UUID
    ): ResponseEntity<TradingOrder>{
        val userId = UUID.fromString(authentication.name)

        return ResponseEntity.ok(
            traderService.executeTrader(
                userId = userId,
                portfolioId = portfolioId,
                traderId = traderId
            )
        )
    }

    //===========================================================//
    //===========================================================//
    // PATCH

    @PatchMapping("/{traderId}/algorithm")
    suspend fun changeAlgorithm(
        authentication: Authentication,
        @PathVariable traderId: UUID,
        @PathVariable portfolioId: UUID,
        @RequestBody request: ChangeTraderAlgorithmRequest
    ): ResponseEntity<TraderResponse> {
        val userId = UUID.fromString(authentication.name)

        return ResponseEntity.ok(
            traderService.changeAlgorithm(
                traderId = traderId,
                userId = userId,
                portfolioId = portfolioId,
                request = request
            )
        )
    }
}