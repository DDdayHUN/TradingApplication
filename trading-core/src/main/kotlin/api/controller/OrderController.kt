package api.controller

import api.dto.OrderResponse
import api.dto.toResponse
import api.service.auth.IAuthenticationService
import application.service.broker.ibkr.InteractiveBrokersService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/portfolio/orders/")
class OrderController(
    private val authService: IAuthenticationService,
    private val brokerService: InteractiveBrokersService
) {
    @GetMapping
    suspend fun getOutStandingOrders(): ResponseEntity<List<OrderResponse>> {
        val response = brokerService.getAllOrder(authService.currentUser())
            .map { order -> order.toResponse() }
            .sortedByDescending { order -> order.timestamp }

        return ResponseEntity.ok(response)
    }
}