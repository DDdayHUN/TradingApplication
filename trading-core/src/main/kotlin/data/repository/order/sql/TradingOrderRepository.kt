package data.repository.order.sql

import data.repository.order.ITradingOrderRepository
import org.springframework.stereotype.Repository

@Repository
class TradingOrderRepository(
    private val repository: ITradingOrderJpaRepository
) : ITradingOrderRepository {

}