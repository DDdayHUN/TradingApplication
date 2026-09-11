package application.service.order

import application.logging.logger
import application.service.broker.IBrokerService
import application.service.trader.ITraderService
import data.repository.order.IOrderRepository
import data.repository.order.sql.toBrokerOrder
import domain.order.Order
import domain.order.Order.Status
import infrastructure.broker.OrderCancelledEvent
import infrastructure.broker.OrderFilledEvent
import infrastructure.broker.OrderSubmittedEvent
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service

@Service
class OrderService(
    private val ibkrService: IBrokerService,
    private val orderRepository: IOrderRepository,
    private val traderService: ITraderService,
) : IOrderService {
    //===========================================================//
    //===========================================================//
    // Private Field(s)

    private val logger = logger<OrderService>()

    //===========================================================//
    //===========================================================//
    // Public Method(s)

    override suspend fun submit(order: Order) {

        val ibkrOrderId = ibkrService.getNextOrderId()

        val persistedOrder = order.withIbkrOrderId(
            ibkrOrderId = ibkrOrderId,
        )

        orderRepository.create(persistedOrder).getOrThrow()

        try {
            ibkrService.placeOrder(ibkrOrderId, order.toBrokerOrder())
        } catch(e: Exception){
            orderRepository.save(persistedOrder.copy(
                status = Status.CANCELLED
            )).getOrThrow()
            throw e
        }
    }

    //===========================================================//

    @Transactional
    override suspend fun handleOrderSubmitted(event: OrderSubmittedEvent) {
        val order = orderRepository.getByIbkrOrderId(event.orderId).getOrThrow()
        orderRepository.save(order.submit().getOrThrow()).getOrThrow()
    }

    //===========================================================//

    @Transactional
    override suspend fun handleOrderCancelled(event: OrderCancelledEvent) {
        val order = orderRepository.getByIbkrOrderId(event.orderId).getOrThrow()
        if(order.status == Status.FILLED) return

        orderRepository.save(order.cancel().getOrThrow()).getOrThrow()
    }

    //===========================================================//

    @Transactional
    override suspend fun handleOrderFilled(event: OrderFilledEvent) {
        val order = orderRepository.getByIbkrOrderId(event.orderId).getOrThrow()
        if(order.status == Status.FILLED) return

        when (val signal = order.signal) {

            is Order.Signal.Buy -> {
                traderService.applyBuyFill(
                    traderId = order.traderId,
                    filledQuantity = event.filled.toInt(),
                    averageFillPrice = event.averageFillPrice
                )
            }

            is Order.Signal.Sell -> {
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

        if (order.signal is Order.Signal.Sell) {
            orderRepository
                .clearOrderAllocation(order.id)
                .getOrThrow()
        }
    }
}