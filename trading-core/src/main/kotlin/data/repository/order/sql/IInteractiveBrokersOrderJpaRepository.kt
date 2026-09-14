package data.repository.order.sql

import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.transaction.annotation.Transactional
import java.util.*

interface IInteractiveBrokersOrderJpaRepository : JpaRepository<InteractiveBrokersOrderEntity, UUID> {
    @EntityGraph(attributePaths = ["sellAllocations"])
    fun findByIbkrOrderId(ibkrOrderId: Int): InteractiveBrokersOrderEntity?

    @Modifying(
        flushAutomatically = true,
        clearAutomatically = true
    )
    @Transactional
    @Query(
        """
        DELETE FROM SellHoldingEntity s
        WHERE s.order.id = :orderId
        """
    )
    fun deleteSellAllocationsByOrderId(
        @Param("orderId") orderId: UUID
    ): Int
}