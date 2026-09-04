package data.repository.order

import domain.order.Order

interface IOrderRepository {
    suspend fun create(order: Order): Result<Order>
    suspend fun save(order: Order): Result<Order>
    suspend fun getByIbkrOrderId(ibkrOrderId: Int): Result<Order>
}