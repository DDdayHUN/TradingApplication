package data.repository.order.sql

import data.repository.portfolio.sql.IPortfolioJpaRepository
import application.service.broker.InteractiveBrokersOrder
import exception.api.TraderNotFoundException
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Repository
class InteractiveBrokersOrderRepository(
    private val orderRepository: IInteractiveBrokersOrderJpaRepository,
    private val portfolioRepository: IPortfolioJpaRepository
) {

    @Transactional
    suspend fun save(ibkrOrder: InteractiveBrokersOrder): Result<InteractiveBrokersOrder> {
        return runCatching {
            val portfolio = portfolioRepository.findByTradersId(ibkrOrder.traderId)
                ?: throw IllegalArgumentException("Portfolio for trader ${ibkrOrder.traderId} not found")

            val trader = portfolio.traders
                .firstOrNull { trader ->
                    trader.id == ibkrOrder.traderId
                } ?: throw TraderNotFoundException(ibkrOrder.traderId)

            orderRepository
                .save(ibkrOrder.toEntity(trader))
                .toInteractiveBrokersOrder()
        }
    }

    @Transactional(readOnly = true)
    suspend fun getByIbkrOrderId(ibkrOrderId: Int): Result<InteractiveBrokersOrder> {
        return runCatching {
            val order = orderRepository.findByIbkrOrderId(ibkrOrderId)
                ?: throw IllegalArgumentException("Order not found with Ibkr Id: ${ibkrOrderId}")

            order.toInteractiveBrokersOrder()
        }
    }

    @Transactional
    suspend fun clearOrderAllocation(orderId: UUID): Result<Unit> {
        return runCatching {
            orderRepository.deleteSellAllocationsByOrderId(orderId)
        }
    }
}