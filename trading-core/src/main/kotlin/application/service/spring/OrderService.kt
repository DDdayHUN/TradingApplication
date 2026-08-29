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
    private val traderService: TraderService,
    private val orderRepository: IOrderRepository,
    private val session: IAuthenticationService,
    private val portfolioService: IPortfolioService,
) : IOrderService {
    override suspend fun submit(order: TradingOrder) {
        val ibkrOrderId = ibkrService.getNextOrderId()
        val user = session.currentUser()
        val portfolio = portfolioService.getPortfolio(order.portfolioId)

        val trader = traderService.getById(
            portfolioId = portfolio.id,
            traderId = order.traderId
        ) ?: throw TraderNotFoundException(order.traderId)

        val orderEntity = order.toEntity(
            trader = trader.toEntity(portfolio.toEntity(user.toEntity())),
            ibkrOrderId = ibkrOrderId
        )

        ibkrService.placeOrder(ibkrOrderId, order.toBrokerOrder()
            ?: throw IllegalArgumentException("Invalid trading order"))

        orderRepository.save(orderEntity)
    }

    @Transactional
    override suspend fun handleOrderSubmitted(event: OrderSubmittedEvent) {
        val order = orderRepository.getByIbkrOrderIdAndTraderId(
            ibkrOrderId = event.orderId,
            traderId =
        )

    }

    override fun handleOrderCancelled(event: OrderCancelledEvent) {
        TODO("Not yet implemented")
    }

    override fun handleOrderFilled(event: OrderFilledEvent) {
        TODO("Not yet implemented")
    }

}