package data.repository.order.sql

import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface ITradingOrderJpaRepository : JpaRepository<TradingOrderEntity, UUID> {

}