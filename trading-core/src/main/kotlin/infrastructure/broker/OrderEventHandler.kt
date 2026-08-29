package infrastructure.broker

import application.service.spring.OrderService
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

@Component
class OrderEventHandler(
    private val orderService: OrderService
) {
    @EventListener
    fun handle(event: OrderSubmittedEvent) {
        orderService.handleOrderSubmitted(event)
    }

    @EventListener
    fun handle(event: OrderFilledEvent){
        orderService.handleOrderFilled(event)
    }

    @EventListener
    fun handle(event: OrderCancelledEvent){
        orderService.handleOrderCancelled(event)
    }
}