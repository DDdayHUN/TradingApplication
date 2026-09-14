package data.repository.order

import domain.order.Order
import java.util.UUID

interface IOrderRepository {
    suspend fun create(order: Order): Result<Order>
    suspend fun save(order: Order): Result<Order>
    suspend fun getByIbkrOrderId(ibkrOrderId: Int): Result<Order>
    suspend fun clearOrderAllocation(orderId: UUID): Result<Unit>
}