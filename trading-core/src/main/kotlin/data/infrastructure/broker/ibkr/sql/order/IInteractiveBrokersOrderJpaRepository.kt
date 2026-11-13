package data.infrastructure.broker.ibkr.sql.order

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.util.*

interface IInteractiveBrokersOrderJpaRepository : JpaRepository<InteractiveBrokersOrderEntity, UUID> {
    @Query(
        """
        SELECT ibkrOrder
        FROM InteractiveBrokersOrderEntity ibkrOrder
        WHERE ibkrOrder.tradingOrder.trader.portfolio.user.id = :userId
        """
    )
    fun findAllForUser(userId: UUID): List<InteractiveBrokersOrderEntity>
}