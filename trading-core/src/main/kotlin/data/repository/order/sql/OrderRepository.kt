package data.repository.order.sql

import data.repository.order.IOrderRepository
import data.repository.portfolio.sql.IPortfolioJpaRepository
import domain.order.Order
import exception.api.TraderNotFoundException
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Repository
class OrderRepository(
    private val orderRepository: IOrderJpaRepository,
    private val portfolioRepository: IPortfolioJpaRepository
) : IOrderRepository {

    @Transactional
    override suspend fun create(order: Order): Result<Order> {
        return runCatching {
            val portfolio = portfolioRepository.findByTradersId(order.traderId)
                ?: throw IllegalArgumentException("Portfolio for trader ${order.traderId} not found")

            val trader = portfolio.traders
                .firstOrNull {trader ->
                    trader.id == order.traderId
                } ?: throw TraderNotFoundException(order.traderId)

            orderRepository
                .save(order.toEntity(trader))
                .toDomain()
        }
    }

    override suspend fun save(order: Order): Result<Order> {
        return runCatching{
            val portfolio = portfolioRepository.findByTradersId(order.traderId)
                ?: throw IllegalArgumentException("Portfolio for trader ${order.traderId} not found")

            val trader = portfolio.traders
                .firstOrNull {trader ->
                    trader.id == order.traderId
                } ?: throw TraderNotFoundException(order.traderId)

            val order =  orderRepository.save(order.toEntity(trader))

            order.toDomain()
        }
    }

    @Transactional(readOnly = true)
    override suspend fun getByIbkrOrderId(ibkrOrderId: Int): Result<Order> {
        return runCatching{
            val order = orderRepository.findByIbkrOrderId(ibkrOrderId)
                ?: throw IllegalArgumentException("Order not found with Ibkr Id: ${ibkrOrderId}")

            order.toDomain()
        }
    }

    @Transactional
    override suspend fun clearOrderAllocation(orderId: UUID): Result<Unit> {
        return runCatching {
            val order = orderRepository.findWithSellAllocationsById(orderId)?:
            throw IllegalArgumentException("Order not found with Id: ${orderId}")

            order.sellAllocations.clear()
        }
    }
}