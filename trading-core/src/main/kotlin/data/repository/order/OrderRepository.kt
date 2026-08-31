package data.repository.order

import data.repository.portfolio.IPortfolioJpaRepository
import domain.interfaces.IOrderRepository
import domain.order.Order
import exception.api.TraderNotFoundException
import jakarta.transaction.Transactional
import org.springframework.stereotype.Repository

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

    override suspend fun getByIbkrOrderId(ibkrOrderId: Int): Result<Order> {
        return runCatching{
            val order = orderRepository.findByIbkrOrderId(ibkrOrderId)
                ?: throw IllegalArgumentException("Order not found with Ibkr Id: ${ibkrOrderId}")

            order.toDomain()
        }
    }
}