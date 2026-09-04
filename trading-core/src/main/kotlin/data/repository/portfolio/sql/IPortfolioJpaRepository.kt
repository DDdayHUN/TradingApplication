package data.repository.portfolio.sql

import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface IPortfolioJpaRepository : JpaRepository<PortfolioEntity, UUID>{
    @EntityGraph(attributePaths = ["traders", "traders.holdings"])
    fun findAllByUserId(userId : UUID) : List<PortfolioEntity>

    @EntityGraph(attributePaths = ["traders", "traders.holdings"])
    fun findByUserIdAndId(userId: UUID, id: UUID): PortfolioEntity?

    @EntityGraph(attributePaths = ["traders", "traders.holdings"])
    fun findByTradersId(traderId: UUID): PortfolioEntity?

    @EntityGraph(attributePaths = ["traders", "traders.holdings"])
    fun findWithRelationsById(id: UUID): PortfolioEntity?
}