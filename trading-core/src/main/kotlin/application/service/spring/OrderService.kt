package application.service.spring

import application.service.IAuthenticationService
import application.service.IOrderService
import application.service.IPortfolioService
import application.service.broker.IBrokerService
import data.repository.order.toBrokerOrder
import data.repository.order.toEntity
import data.repository.portfolio.toEntity
import data.repository.trader.toEntity
import data.repository.user.toEntity
import domain.interfaces.IOrderRepository
import domain.order.OrderStatus
import domain.order.toOrder
import domain.trader.TradingOrder
import exception.api.TraderNotFoundException
import infrastructure.broker.OrderCancelledEvent
import infrastructure.broker.OrderFilledEvent
import infrastructure.broker.OrderSubmittedEvent
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service

@Service
class OrderService(
    private val ibkrService: IBrokerService,
    private val orderRepository: IOrderRepository,
) : IOrderService {

    override suspend fun submit(order: TradingOrder) {
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
    override suspend fun handleOrderFilled(event: OrderFilledEvent) {
        val order = orderRepository.getByIbkrOrderId(event.orderId).getOrThrow()
        if(order.status == OrderStatus.FILLED) return

        orderRepository.save(order.filled(
            filledQuantity = event.filled,
            averageFillPrice = event.averageFillPrice
        )).getOrThrow()

        //TODO holding handling
    }

}