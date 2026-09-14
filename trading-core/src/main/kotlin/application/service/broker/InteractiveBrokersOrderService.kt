package application.service.broker

import application.logging.logger
import application.service.trader.ITraderService
import data.repository.order.sql.InteractiveBrokersOrderRepository
import domain.trader.TradingOrder
import infrastructure.broker.IbkrEvent
import jakarta.transaction.Transactional
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Service

@Service
class InteractiveBrokersOrderService(
    private val brokerService: InteractiveBrokersService,
    private val orderRepository: InteractiveBrokersOrderRepository,
    private val traderService: ITraderService,
) {
    //===========================================================//
    //===========================================================//
    // Private Field(s)

    private val logger = logger<InteractiveBrokersOrderService>()

    //===========================================================//
    //===========================================================//
    // Public Method(s)

    suspend fun submit(order: TradingOrder) {
        val persistedOrders = order.toInteractiveBrokersOrder(brokerService)

        persistedOrders.forEach { order ->
            orderRepository.save(order).getOrThrow()
        }

        persistedOrders.forEach { order ->
            try {
                brokerService.placeOrder(order)
            } catch(e: Exception){
                orderRepository.save(order.copy(
                    status = InteractiveBrokersOrder.Status.CANCELLED
                )).getOrThrow()
                throw e
            }
        }
    }

    //===========================================================//

    @Transactional
    @EventListener
    suspend fun handle(event: IbkrEvent.OrderSubmittedEvent) {
        val order = orderRepository.getByIbkrOrderId(event.orderId).getOrThrow()
        if(order.status != InteractiveBrokersOrder.Status.PENDING) return
        orderRepository.save(order.submit().getOrThrow()).getOrThrow()
    }

    //===========================================================//

    @Transactional
    @EventListener
    suspend fun handle(event: IbkrEvent.OrderCancelledEvent) {
        val order = orderRepository.getByIbkrOrderId(event.orderId).getOrThrow()
        if(order.status == InteractiveBrokersOrder.Status.FILLED) return

        orderRepository.save(order.cancel().getOrThrow()).getOrThrow()
    }

    //===========================================================//

    @Transactional
    @EventListener
    suspend fun handle(event: IbkrEvent.OrderFilledEvent) {
        val order = orderRepository.getByIbkrOrderId(event.orderId).getOrThrow()
        if(order.status == InteractiveBrokersOrder.Status.FILLED) return

        when (val signal = order.signal) {

            is InteractiveBrokersOrder.Signal.Buy -> {
                traderService.applyBuyFill(
                    traderId = order.traderId,
                    filledQuantity = event.filled.toInt(),
                    averageFillPrice = event.averageFillPrice
                )
            }

            is InteractiveBrokersOrder.Signal.Sell -> {
                traderService.applySellFill(
                    traderId = order.traderId,
                    sellAllocations = signal.allocations,
                    averageFillPrice = event.averageFillPrice
                )
            }
        }

        logger.info(
            "Applying filled order orderId={} quantity={} avgPrice={}",
            event.orderId,
            event.filled,
            event.averageFillPrice
        )

        val filledOrder = order.fill(
            filledQuantity = event.filled,
            averageFillPrice = event.averageFillPrice
        ).getOrThrow()

        orderRepository.save(filledOrder).getOrThrow()

        if (order.signal is InteractiveBrokersOrder.Signal.Sell) {
            orderRepository
                .clearOrderAllocation(filledOrder.id)
                .getOrThrow()
        }
    }
}