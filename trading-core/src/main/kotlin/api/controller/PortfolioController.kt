package api.controller

import api.dto.PortfolioResponse
import api.dto.toResponse
import api.service.auth.IAuthenticationService
import application.service.broker.IBrokerService
import application.service.portfolio.IPortfolioService
import application.service.user.IUserService
import data.repository.portfolio.sql.toDomain
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

//===========================================================//
//===========================================================//

@RestController
@RequestMapping("/api/portfolio/")
class PortfolioController(
    private val authService: IAuthenticationService,
    private val userService: IUserService,
    private val brokerService: IBrokerService
) {
    //===========================================================//
    //===========================================================//
    // GET

    @GetMapping
    suspend fun getPortfolio(): ResponseEntity<PortfolioResponse> {
        val userId = authService.currentUser()
        val userEntity = userService.getUserById(userId)
        val accSum = brokerService.getAccountSummary(userId)

        val response = userEntity.portfolio
            .toDomain()
            .toResponse(accSum.availableCapital, ibkrNetLiquidation = accSum.netLiquidation)

        return ResponseEntity.ok(
            response
        )
    }
}