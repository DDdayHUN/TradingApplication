package api.controller

import api.dto.OrderResponse
import api.dto.toResponse
import application.service.broker.InteractiveBrokersOrderService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/portfolio/orders/")
class OrderController(
    private val orderService: InteractiveBrokersOrderService,
) {
    @GetMapping
    suspend fun getAll(): ResponseEntity<List<OrderResponse>>{
        val response = orderService.getAll().map {
            order -> order.toResponse()
        }
        return ResponseEntity.ok(response)
    }
}