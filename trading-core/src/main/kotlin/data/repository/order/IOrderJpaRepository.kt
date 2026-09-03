package data.repository.order

import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface IOrderJpaRepository : JpaRepository<OrderEntity, UUID> {
    @EntityGraph(attributePaths = ["sellAllocations"])
    fun findByIbkrOrderId(ibkrOrderId: Int): OrderEntity?
}