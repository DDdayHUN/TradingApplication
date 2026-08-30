package data.repository.order

import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface IOrderJpaRepository : JpaRepository<OrderEntity, UUID> {
    fun findByIbkrOrderId(ibkrOrderId: Int): OrderEntity?
}