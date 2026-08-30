package application.service.spring

import application.service.IOrderService
import application.service.ITraderService
import application.service.broker.IBrokerService
import data.repository.order.toBrokerOrder
import domain.interfaces.IOrderRepository
import domain.order.OrderStatus
import domain.order.toOrder
import domain.trader.TradingOrder
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

    override suspend fun submit(order: TradingOrder) {
        if(order.buy == null && order.sell == null) return

        val ibkrOrderId = ibkrService.getNextOrderId()

        val persistedOrder = order.toOrder(
            ibkrOrderId = ibkrOrderId,
        )
        orderRepository.create(persistedOrder).getOrThrow()

        try {
            ibkrService.placeOrder(ibkrOrderId, order.toBrokerOrder()!!)
        } catch(e: Exception){
            orderRepository.save(persistedOrder.copy(
                status = OrderStatus.CANCELLED
            )).getOrThrow()
            throw e
        }
    }

    @Transactional
    override suspend fun handleOrderSubmitted(event: OrderSubmittedEvent) {
        val order = orderRepository.getByIbkrOrderId(event.orderId).getOrThrow()
        orderRepository.save(order.submitted()).getOrThrow()
    }

    @Transactional
    override suspend fun handleOrderCancelled(event: OrderCancelledEvent) {
        val order = orderRepository.getByIbkrOrderId(event.orderId).getOrThrow()
        if(order.status == OrderStatus.FILLED) return

        orderRepository.save(order.cancelled()).getOrThrow()
    }

    @Transactional
    @Deprecated("Ez most nem jo mert csak buyt tud csinalni sellnel meg meg kell csinalni")
    override suspend fun handleBuyOrderFilled(event: OrderFilledEvent) {
        val order = orderRepository.getByIbkrOrderId(event.orderId).getOrThrow()
        if(order.status == OrderStatus.FILLED) return

        traderService.applyBuyFill(
            traderId = order.traderId,
            filledQuantity = event.filled.toInt(),
            averageFillPrice = event.averageFillPrice
        )

        orderRepository.save(order.filled(
            filledQuantity = event.filled,
            averageFillPrice = event.averageFillPrice
        )).getOrThrow()
    }
}