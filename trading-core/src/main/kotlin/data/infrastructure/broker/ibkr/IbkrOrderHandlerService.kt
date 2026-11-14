package data.infrastructure.broker.ibkr

import application.logging.logger
import application.service.trader.ITraderService
import data.infrastructure.broker.ibkr.InteractiveBrokersOrder.Status
import data.infrastructure.broker.ibkr.sql.order.IInteractiveBrokersOrderRepository
import data.repository.order.sql.TradingOrderEntity
import data.repository.security.toDomain
import kotlinx.coroutines.runBlocking
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

//===========================================================//
//===========================================================//
// NOTE : This class is purposefully left unmanaged by Spring,
//        we instantiate and manage this class ourselves,
//        also this class is marked 'open' because of @Transactional.

open class IbkrOrderHandlerService(
    private val traderService: ITraderService,
    private val ibkrOrderRepository: IInteractiveBrokersOrderRepository
) {
    // TODO : Redo this class's event handlers, because they are half implemented.
    //===========================================================//
    //===========================================================//
    // Private Field(s)

    private val m_Logger = logger<IbkrOrderHandlerService>()

    //===========================================================//
    //===========================================================//
    // Public Method(s)

    @Transactional
    open fun handle(orderEvent: OrderEvent) = runBlocking {
        when (orderEvent) {
            is OrderEvent.Pending       -> orderPending(orderEvent)

            is OrderEvent.PendingSubmit -> orderPendingSubmit(orderEvent)
            is OrderEvent.Submitted     -> orderSubmitted(orderEvent)

            is OrderEvent.PendingCancel -> orderPendingCancel(orderEvent)
            is OrderEvent.Cancelled     -> orderCancelled(orderEvent)

            is OrderEvent.Filled        -> orderFilled(orderEvent)
        }
    }

    //===========================================================//
    //===========================================================//
    // Private Method(s)

    private suspend fun orderPending(event: OrderEvent.Pending) {
        val order = ibkrOrderRepository.getById(event.userId).getOrThrow()
        if(order.status != Status.PENDING) return

        m_Logger.info("IBKR order pending with ID:{${order.tradingOrder.id}}")
    }

    //===========================================================//

    private suspend fun orderPendingSubmit(event: OrderEvent.PendingSubmit) {
        val order = ibkrOrderRepository.getById(event.userId).getOrThrow()
        if(order.status != Status.PENDING) return

        order.status = Status.PRE_SUBMITTED
        ibkrOrderRepository.save(order).getOrThrow()

        m_Logger.info("IBKR order pre-submitted with ID:{${order.tradingOrder.id}}")
    }

    //===========================================================//

    private suspend fun orderSubmitted(event: OrderEvent.Submitted) {
        val order = ibkrOrderRepository.getById(event.userId).getOrThrow()
        if (order.status == Status.FILLED       ||
            order.status == Status.CANCELLED    ||
            order.status == Status.SUBMITTED
        ) return

        order.status = Status.SUBMITTED
        ibkrOrderRepository.save(order).getOrThrow()

        m_Logger.info("IBKR order submitted with ID:{${order.tradingOrder.id}}")
    }

    //===========================================================//

    private suspend fun orderPendingCancel(event: OrderEvent.PendingCancel) {
        val order = ibkrOrderRepository.getById(event.userId).getOrThrow()

        if (order.status == Status.FILLED           ||
            order.status == Status.CANCELLED        ||
            order.status == Status.PENDING_CANCEL
        ) return

        order.status = Status.PENDING_CANCEL

        ibkrOrderRepository.save(order).getOrThrow()

        m_Logger.info("IBKR order pending cancellation with ID:{${order.tradingOrder.id}}")
    }

    //===========================================================//

    private suspend fun orderCancelled(event: OrderEvent.Cancelled) {
        val order = ibkrOrderRepository.getById(event.userId).getOrThrow()
        if(order.status == Status.FILLED) return

        order.status = Status.CANCELLED
        ibkrOrderRepository.save(order).getOrThrow()

        m_Logger.info("IBKR order cancelled with ID:{${order.tradingOrder.id}}")
    }

    //===========================================================//

    private suspend fun orderFilled(event: OrderEvent.Filled) {
        val order = ibkrOrderRepository.getById(event.userId).getOrThrow()
        if(order.status != Status.PENDING || order.status != Status.SUBMITTED) return

        when (order.tradingOrder.action) {
            TradingOrderEntity.Action.BUY -> {
                traderService.applyBuyFill(
                    traderId = order.tradingOrder.trader.id,
                    filledQuantity = event.filled.toInt(),
                    averageFillPrice = event.averageFillPrice
                )
            }
            TradingOrderEntity.Action.SELL -> {
                traderService.applySellFill(
                    traderId = order.tradingOrder.trader.id,
                    sellAllocations = order.tradingOrder.sellHoldings!!.map{ it.toDomain() }.toSet(),
                    averageFillPrice = event.averageFillPrice
                )
            }
        }

        m_Logger.info("IBKR order filled with ID:{${order.tradingOrder.id}}, Quantity:{${event.filled}}, Average Fill Price:{${event.averageFillPrice}}")

        order.filledQuantity = event.filled
        order.averageFillPrice = event.averageFillPrice
        order.status = Status.FILLED

        ibkrOrderRepository.save(order).getOrThrow()
    }

    //===========================================================//
    //===========================================================//
    // Helper Class(es)

    sealed interface OrderEvent {
        val userId: UUID
        val orderId: Int

        data class Pending(override val userId: UUID, override val orderId: Int): OrderEvent

        data class PendingSubmit(override val userId: UUID, override val orderId: Int): OrderEvent
        data class Submitted(override val userId: UUID, override val orderId: Int): OrderEvent

        data class PendingCancel(override val userId: UUID, override val orderId: Int): OrderEvent
        data class Cancelled(override val userId: UUID, override val orderId: Int): OrderEvent

        data class Filled(override val userId: UUID, override val orderId: Int, val filled: String, val averageFillPrice: Double): OrderEvent
    }
}