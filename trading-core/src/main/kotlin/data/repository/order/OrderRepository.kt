package data.repository.order

import domain.interfaces.IOrderRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
class OrderRepository(
    private val orderRepository: IOrderJpaRepository
) : IOrderRepository {
    override suspend fun save(order: OrderEntity): Result<OrderEntity> {
        return runCatching {
            orderRepository.save(order)
        }
    }

    override suspend fun getByIbkrOrderIdAndTraderId(
        ibkrOrderId: Int,
        traderId: UUID
    ): Result<OrderEntity> {
        return runCatching {
            orderRepository.findByIbkrOrderIdAndTraderId(
                ibkrOrderId = ibkrOrderId,
                traderId = traderId
            ) ?: throw IllegalArgumentException("Order not found")
        }
    }
}