package api.controller

import api.dto.PortfolioResponse
import api.dto.toResponse
import api.service.IControllerService
import api.service.auth.IAuthenticationService
import application.service.broker.IBrokerService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

//===========================================================//
//===========================================================//

@RestController
@RequestMapping("/api/portfolio/")
class PortfolioController(
    private val authService: IAuthenticationService,
    private val controllerService: IControllerService,
    private val brokerService: IBrokerService
) {
    //===========================================================//
    //===========================================================//
    // GET

    @GetMapping
    suspend fun getPortfolio(): ResponseEntity<PortfolioResponse> {
        val userId = authService.currentUser()
        val accSum = brokerService.getAccountSummary(userId)

        val response = controllerService.getPortfolio(userId)
            .toResponse(accSum.availableCapital, ibkrNetLiquidation = accSum.netLiquidation)

        return ResponseEntity.ok(
            response
        )
    }
}