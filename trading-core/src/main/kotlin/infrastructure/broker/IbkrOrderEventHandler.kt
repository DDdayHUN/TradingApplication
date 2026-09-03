package infrastructure.broker

import application.service.order.OrderService
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

@Component
class IbkrOrderEventHandler(
    private val orderService: OrderService
) {

    @EventListener
    suspend fun handle(event: OrderSubmittedEvent) {
        return orderService.handleOrderSubmitted(event)
    }

    @EventListener
    suspend fun handle(event: OrderFilledEvent){
        orderService.handleOrderFilled(event)
    }

    @EventListener
    suspend fun handle(event: OrderCancelledEvent){
        orderService.handleOrderCancelled(event)
    }
}