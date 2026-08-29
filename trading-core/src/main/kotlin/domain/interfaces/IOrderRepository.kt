package domain.interfaces

import data.repository.order.OrderEntity
import java.util.UUID

interface IOrderRepository {
    suspend fun save(order: OrderEntity): Result<OrderEntity>
    suspend fun getByIbkrOrderIdAndTraderId(ibkrOrderId: Int, traderId: UUID): Result<OrderEntity>
}