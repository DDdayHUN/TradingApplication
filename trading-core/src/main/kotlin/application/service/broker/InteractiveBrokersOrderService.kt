package application.service.broker

import application.logging.logger
import application.service.trader.ITraderService
import data.repository.order.sql.InteractiveBrokersOrderRepository
import domain.trader.TradingOrder
import data.infrastructure.broker.IbkrEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Service
import java.util.*

@Service
class InteractiveBrokersOrderService(
    private val brokerService: InteractiveBrokersService,
    private val traderService: ITraderService,
    private val orderRepository: InteractiveBrokersOrderRepository
) {
    //===========================================================//
    //===========================================================//
    // Private Field(s)

    private val m_Logger = logger<InteractiveBrokersOrderService>()

    //===========================================================//
    //===========================================================//
    // Public Method(s)

    suspend fun submit(userId: UUID, order: TradingOrder) {
        val persistedOrders = order.toInteractiveBrokersOrder(userId,brokerService)

        persistedOrders.forEach { order ->
            orderRepository.save(order).getOrThrow()
        }

        persistedOrders.forEach { order ->
            try {
                brokerService.placeOrder(userId,order)
            } catch(e: Exception){
                orderRepository.save(order.copy(
                    status = InteractiveBrokersOrder.Status.CANCELLED
                )).getOrThrow()
                throw e
            }
        }
    }

    //===========================================================//

    suspend fun getAll(userId: UUID): List<InteractiveBrokersOrder>{
        return orderRepository.getAll(userId).getOrThrow()
    }

    //===========================================================//

    @EventListener
    suspend fun handle(event: IbkrEvent.OrderSubmittedEvent) {
        val order = orderRepository.getByIbkrOrderId(event.orderId).getOrThrow()
        if (order.status == InteractiveBrokersOrder.Status.FILLED ||
            order.status == InteractiveBrokersOrder.Status.CANCELLED ||
            order.status == InteractiveBrokersOrder.Status.SUBMITTED
        ) return
        orderRepository.save(order.submit().getOrThrow()).getOrThrow()
    }

    //===========================================================//

    @EventListener
    suspend fun handle(event: IbkrEvent.OrderCancelledEvent) {
        val order = orderRepository.getByIbkrOrderId(event.orderId).getOrThrow()
        if(order.status == InteractiveBrokersOrder.Status.FILLED) return

        orderRepository.save(order.cancel().getOrThrow()).getOrThrow()
    }

    //===========================================================//

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

        m_Logger.info(
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

    //===========================================================//

    @EventListener
    suspend fun handle(event: IbkrEvent.OrderPreSubmittedEvent){
        val order = orderRepository.getByIbkrOrderId(event.orderId).getOrThrow()
        if(order.status != InteractiveBrokersOrder.Status.PENDING) return

        orderRepository.save(
            order.copy(
                status = InteractiveBrokersOrder.Status.PRE_SUBMITTED
            )
        ).getOrThrow()

        m_Logger.info("IBKR order pre-submitted orderId=${event.orderId}")
    }

    //===========================================================//

    @EventListener
    suspend fun handle(event: IbkrEvent.OrderPendingEvent){
        val order = orderRepository.getByIbkrOrderId(event.orderId).getOrThrow()
        if(order.status != InteractiveBrokersOrder.Status.PENDING) return

        m_Logger.info("IBKR order pending orderId=${event.orderId}")
    }

    //===========================================================//

    @EventListener
    suspend fun handle(event: IbkrEvent.OrderPendingCancelEvent){
        val order = orderRepository.getByIbkrOrderId(event.orderId).getOrThrow()
        if (order.status == InteractiveBrokersOrder.Status.FILLED ||
            order.status == InteractiveBrokersOrder.Status.CANCELLED ||
            order.status == InteractiveBrokersOrder.Status.PENDING_CANCEL
        ) return

        orderRepository.save(
            order.copy(
                status = InteractiveBrokersOrder.Status.PENDING_CANCEL
            )
        ).getOrThrow()

        m_Logger.info("IBKR order pending cancellation orderId=${event.orderId}")
    }

    //===========================================================//

    @EventListener
    suspend fun handle(event: IbkrEvent.OrderInactiveEvent){
        val order = orderRepository.getByIbkrOrderId(event.orderId).getOrThrow()
        if (order.status == InteractiveBrokersOrder.Status.FILLED ||
            order.status == InteractiveBrokersOrder.Status.CANCELLED ||
            order.status == InteractiveBrokersOrder.Status.INACTIVE
        ) return

        orderRepository.save(
            order.copy(
                status = InteractiveBrokersOrder.Status.INACTIVE
            )
        ).getOrThrow()

        m_Logger.warn("IBKR order inactive orderId=${event.orderId}")
    }
}