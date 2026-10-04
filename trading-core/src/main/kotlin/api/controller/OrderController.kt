package api.controller

import api.dto.OrderResponse
import api.dto.toResponse
import api.service.auth.IAuthenticationService
import application.service.broker.InteractiveBrokersOrderService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/portfolio/orders/")
class OrderController(
    private val orderService: InteractiveBrokersOrderService,
    private val authService: IAuthenticationService
) {
    @GetMapping
    suspend fun getAll(): ResponseEntity<List<OrderResponse>> {
        val response = orderService.getAll(authService.currentUser()).map {
            order -> order.toResponse()
        }
        return ResponseEntity.ok(response)
    }
}