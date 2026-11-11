package data.infrastructure.broker.ibkr.order.sql

import data.repository.portfolio.sql.IPortfolioJpaRepository
import data.infrastructure.broker.ibkr.InteractiveBrokersOrder
import exception.TraderNotFoundException
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Repository
class InteractiveBrokersOrderRepository(
    private val orderRepository: IInteractiveBrokersOrderJpaRepository,
    private val portfolioRepository: IPortfolioJpaRepository
) {

    @Transactional
    suspend fun save(ibkrOrder: InteractiveBrokersOrderEntity): Result<InteractiveBrokersOrderEntity> {
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

    // TODO : Ez nem jo, mivel orderId userhez kotott es nem unique. Torlunk olyan ordert is amit nem kene.
    @Transactional(readOnly = true)
    suspend fun getByIbkrOrderId(ibkrOrderId: Int): Result<InteractiveBrokersOrderEntity> {
        return runCatching {
            val order = orderRepository.findByIbkrOrderId(ibkrOrderId)
                ?: throw IllegalArgumentException("Order not found with Ibkr Id: ${ibkrOrderId}")

            order
        }
    }

    // TODO : Ez nem jo, mivel orderId userhez kotott es nem unique. Torlunk olyan ordert is amit nem kene.
    @Transactional
    suspend fun clearOrderAllocation(orderId: UUID): Result<Unit> {
        return runCatching {
            orderRepository.deleteSellAllocationsByOrderId(orderId)
        }
    }

    @Transactional(readOnly = true)
    suspend fun getAll(userId: UUID): Result<List<InteractiveBrokersOrderEntity>>{
        return runCatching{
            orderRepository.findAllByTraderPortfolioUserId(userId)
        }
    }
}