package data.repository.portfolio

import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface IPortfolioJpaRepository : JpaRepository<PortfolioEntity, UUID>{
    fun findAllByUserId(userId : UUID) : List<PortfolioEntity>
}